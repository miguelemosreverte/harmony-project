package harmonia.stories.read

import io.circe.Json
import org.commonmark.node.{FencedCodeBlock, Heading, Text}
import org.commonmark.parser.Parser
import org.snakeyaml.engine.v2.api.{Dump, DumpSettings, Load, LoadSettings}
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.api.lowlevel.Parse
import org.snakeyaml.engine.v2.events.NodeEvent
import scala.jdk.CollectionConverters.*
import scala.util.Try

object MarkdownYaml:
  def read(markdown: String, section: String): Either[String, Json] =
    val document = Parser.builder().build().parse(markdown)
    val blocks = Iterator
      .iterate(document.getFirstChild)(_.getNext)
      .takeWhile(_ != null)
      .collect {
        case heading: Heading
            if heading.getLevel == 2 && heading.getFirstChild.isInstanceOf[Text] &&
              heading.getFirstChild.asInstanceOf[Text].getLiteral == section =>
          heading.getNext
      }
      .toVector
    blocks match
      case Vector(block: FencedCodeBlock) if block.getInfo.trim == "yaml" =>
        parseYaml(block.getLiteral)
      case _ => Left(s"Expected exactly one YAML block immediately below '## $section'")

  private def parseYaml(text: String): Either[String, Json] =
    val settings = LoadSettings
      .builder()
      .setAllowDuplicateKeys(false)
      .setMaxAliasesForCollections(0)
      .setCodePointLimit(262144)
      .build()
    Try {
      val anchors = new Parse(settings).parseString(text).asScala.exists {
        case event: NodeEvent => event.getAnchor.isPresent
        case _                => false
      }
      require(!anchors, "Write values explicitly; YAML anchors and aliases are not supported")
      new Load(settings).loadFromString(text)
    }.toEither.left.map(_.getMessage).flatMap(toJson)

  private def toJson(value: Any): Either[String, Json] = value match
    case null                     => Right(Json.Null)
    case text: String             => Right(Json.fromString(text))
    case bool: java.lang.Boolean  => Right(Json.fromBoolean(bool))
    case number: java.lang.Number => io.circe.parser.parse(number.toString).left.map(_.message)
    case list: java.util.List[?] =>
      list.asScala
        .foldLeft[Either[String, Vector[Json]]](Right(Vector.empty))((acc, item) =>
          for items <- acc; json <- toJson(item) yield items :+ json
        )
        .map(Json.fromValues)
    case map: java.util.Map[?, ?] =>
      map.asScala.toVector
        .foldLeft[Either[String, Vector[(String, Json)]]](Right(Vector.empty)) {
          case (acc, (key, item)) =>
            val name: Either[String, String] = (key: Any) match
              case text: String => Right(text)
              case _            => Left("YAML mapping keys must be text")
            for items <- acc; field <- name; json <- toJson(item) yield items :+ (field -> json)
        }
        .map(Json.fromFields)
    case _ => Left("Unsupported YAML value")

  def render(title: String, section: String, value: Json): String =
    val settings = DumpSettings
      .builder()
      .setDefaultFlowStyle(FlowStyle.BLOCK)
      .setIndent(2)
      .setIndicatorIndent(2)
      .setIndentWithIndicator(true)
      .build()
    val yaml = new Dump(settings).dumpToString(toJava(value))
    s"# $title\n\n## $section\n\n```yaml\n${yaml.stripTrailing()}\n```\n"

  private def toJava(value: Json): Object = value.fold(
    null,
    bool => java.lang.Boolean.valueOf(bool),
    number =>
      number.toLong match
        case Some(value) => java.lang.Long.valueOf(value)
        case None        => number.toBigDecimal.get.bigDecimal,
    identity,
    values => values.map(toJava).asJava,
    fields =>
      val result = new java.util.LinkedHashMap[String, Object]()
      fields.toIterable.foreach((key, item) => result.put(key, toJava(item)))
      result
  )
