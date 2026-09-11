package harmonia.book.atlas

import cats.effect.IO
import harmonia.files.ArtifactFiles
import io.circe.syntax.*
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import org.snakeyaml.engine.v2.api.{Load, LoadSettings}
import scala.jdk.CollectionConverters.*

/** Reads manifest data with the YAML parser. No Scala or Daml source matching is involved. */
object PackageManifests:
  final case class Manifest(
      file: String,
      sha256: String,
      name: String,
      version: String,
      dependencies: Vector[String],
      imports: Vector[String]
  )
  private given io.circe.Encoder[Manifest] =
    io.circe.Encoder.forProduct6("file", "sha256", "name", "version", "dependencies", "imports")(
      m => (m.file, m.sha256, m.name, m.version, m.dependencies, m.imports)
    )

  def decode(file: String, text: String): Manifest =
    val settings = LoadSettings
      .builder()
      .setAllowDuplicateKeys(false)
      .setMaxAliasesForCollections(0)
      .setCodePointLimit(65536)
      .build()
    val values = new Load(settings).loadFromString(text) match
      case mapping: java.util.Map[?, ?] => mapping.asScala.toMap
      case _ => throw IllegalArgumentException("Expected a YAML mapping: " + file)
    def field(name: String): Any = values.collectFirst {
      case (key: String, value) if key == name => value
    }.orNull
    def string(name: String): String = field(name) match
      case value: String if value.nonEmpty => value
      case _ => throw IllegalArgumentException(s"Missing text field $name: $file")
    def strings(name: String): Vector[String] = field(name) match
      case null => Vector.empty
      case items: java.util.List[?] =>
        items.asScala.toVector.map {
          case value: String => value
          case _             => throw IllegalArgumentException(s"Expected text in $name: $file")
        }
      case _ => throw IllegalArgumentException(s"Expected a list for $name: $file")
    val digest = MessageDigest
      .getInstance("SHA-256")
      .digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8))
      .map(b => f"${b & 0xff}%02x")
      .mkString
    Manifest(
      file,
      digest,
      string("name"),
      string("version"),
      strings("dependencies"),
      strings("data-dependencies")
    )

  def read(root: Path): IO[Vector[Manifest]] = IO.blocking {
    val stream = Files.walk(root.resolve("product/ledger"))
    try
      stream
        .iterator()
        .asScala
        .filter(path =>
          path.getFileName.toString == "daml.yaml" && !Files.isSymbolicLink(path) && !root
            .relativize(path)
            .iterator()
            .asScala
            .exists(_.toString == ".daml")
        )
        .toVector
        .sortBy(_.toString)
        .map(path => decode(root.relativize(path).toString, Files.readString(path)))
    finally stream.close()
  }

  def write(root: Path, output: Path): IO[Unit] =
    read(root).flatMap(manifests => ArtifactFiles.write(output, manifests.asJson.spaces2 + "\n"))
