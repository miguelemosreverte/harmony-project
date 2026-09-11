package harmonia.packages.workspace

import harmonia.packages.InspectedPackage
import harmonia.packages.read.PackageInput
import io.circe.{Decoder, Json}
import java.nio.file.Path

private[workspace] final case class InspectedDar(
    packageId: String,
    lf: String,
    packages: Map[String, Json]
)
private[workspace] object InspectedDar:
  given Decoder[InspectedDar] =
    Decoder.forProduct3("main_package_id", "lf", "packages")(InspectedDar.apply)

private[workspace] enum SupportedBinding(
    val source: String,
    val mapping: String,
    val availableLive: Boolean
):
  case Financing extends SupportedBinding("legacy-financing", "financing", true)
  case Primitive extends SupportedBinding("primitive-approval", "primitive-approval", false)

  def diagnostic: String = this match
    case Financing => "Reviewed generated approval is available in the workflow action menu."
    case Primitive =>
      "A reviewed primitive-action mapping can generate a portable project. Register its typed action and rebuild to add it to the live composer."

private[workspace] final case class CompiledProject(archive: Path, manifest: Json)

/** Inspection and compilation state. HTTP flags are derived from these facts. */
private[workspace] final case class BuilderInput(
    id: String,
    directory: Path,
    origin: String,
    sha256: String,
    inspection: InspectedDar,
    source: Option[PackageInput],
    project: Option[CompiledProject] = None
):
  def binding: Option[SupportedBinding] =
    source.flatMap(pin => SupportedBinding.values.find(_.source == pin.name))

  def view: InspectedPackage = InspectedPackage(
    id,
    origin,
    sha256,
    inspection.packageId,
    inspection.lf,
    source.map(_.name),
    binding.nonEmpty,
    binding.exists(_.availableLive),
    project.nonEmpty,
    binding.fold(
      "No reviewed executable mapping matches this input. Package inspection does not make an action available; add a pinned source and supported typed mapping, then rebuild."
    )(_.diagnostic),
    inspection.packages,
    project.map(_.manifest)
  )
