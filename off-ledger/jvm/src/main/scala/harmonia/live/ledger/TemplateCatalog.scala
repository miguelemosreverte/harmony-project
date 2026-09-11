package harmonia.live.ledger

import cats.effect.IO
import cats.syntax.all.*
import harmonia.packages.inspect.InspectDar
import java.nio.file.Path

final case class TemplateCatalog(packages: Map[String, String]):
  def accepts(contract: ActiveContract): Boolean =
    TemplateCatalog.owners
      .get(contract.template.getModuleName)
      .flatMap(packages.get)
      .contains(contract.template.getPackageId)

object TemplateCatalog:
  private val owners = Map(
    "PrivateFinancing" -> "private-financing",
    "Harmonia.SharedProgress" -> "harmonia-core",
    "Harmonia.Process.Engine" -> "harmonia-core",
    "Harmonia.Result" -> "harmonia-interfaces",
    "Composer" -> "harmonia-composer",
    "Financing" -> "harmonia-financing",
    "Review" -> "harmonia-review",
    "LegacyFinancing" -> "legacy-financing",
    "GeneratedFinancing" -> "harmonia-binding-generatedfinancing"
  )
  def load(root: Path, dar: Path, output: Path): IO[TemplateCatalog] = for
    inspection <- InspectDar.inspect(root, dar, output)
    entries <- IO.fromEither(inspection.hcursor.get[Map[String, io.circe.Json]]("packages"))
    ids <- owners.values.toVector.distinct.traverse { name =>
      val matching = entries.filter(_._2.hcursor.get[String]("name").contains(name)).keys.toVector
      IO.raiseUnless(matching.size == 1)(
        IllegalArgumentException(s"Expected one compiled package named $name")
      ).flatMap(_ => IO.pure(name -> matching.head))
    }
  yield TemplateCatalog(ids.toMap)
