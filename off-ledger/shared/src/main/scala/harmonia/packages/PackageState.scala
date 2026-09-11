package harmonia.packages

import io.circe.Decoder

final case class PackageSource(id: String, source: String)
object PackageSource:
  given Decoder[PackageSource] = Decoder.forProduct2("id", "source")(PackageSource.apply)
final case class InspectedPackage(
    id: String,
    origin: String,
    sha256: String,
    packageId: String,
    lf: String,
    matchedSource: Option[String],
    canGenerate: Boolean,
    availableLive: Boolean,
    compiled: Boolean,
    diagnostic: String
)
object InspectedPackage:
  given Decoder[InspectedPackage] = Decoder.forProduct10(
    "id",
    "origin",
    "sha256",
    "package_id",
    "lf",
    "matched_source",
    "can_generate",
    "available_live",
    "compiled",
    "diagnostic"
  )(InspectedPackage.apply)
final case class PackageState(
    inputs: Vector[InspectedPackage],
    remaining: Int,
    sources: Vector[PackageSource]
)
object PackageState:
  val empty = PackageState(Vector.empty, 8, Vector.empty)
  given Decoder[PackageState] =
    Decoder.forProduct3("inputs", "remaining", "sources")(PackageState.apply)
