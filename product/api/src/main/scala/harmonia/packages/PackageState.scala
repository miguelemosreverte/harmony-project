package harmonia.packages

import io.circe.{Codec, Decoder, Encoder, Json}

final case class PackageSource(id: String, source: String)
object PackageSource:
  given Codec.AsObject[PackageSource] =
    Codec.forProduct2("id", "source")(PackageSource.apply)(v => (v.id, v.source))

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
    diagnostic: String,
    includedPackages: Map[String, Json],
    generation: Option[Json]
)
object InspectedPackage:
  private val fields: Codec.AsObject[InspectedPackage] = Codec.forProduct12(
    "id",
    "origin",
    "sha256",
    "package_id",
    "lf",
    "matched_source",
    "can_generate",
    "available_live",
    "compiled",
    "diagnostic",
    "packages",
    "generation"
  )(InspectedPackage.apply)(v =>
    (
      v.id,
      v.origin,
      v.sha256,
      v.packageId,
      v.lf,
      v.matchedSource,
      v.canGenerate,
      v.availableLive,
      v.compiled,
      v.diagnostic,
      v.includedPackages,
      v.generation
    )
  )
  given Decoder[InspectedPackage] = fields
  // The existing API publishes a generation manifest only after compilation.
  given Encoder.AsObject[InspectedPackage] = Encoder.AsObject.instance { value =>
    val encoded = fields.encodeObject(value)
    if value.generation.isDefined then encoded else encoded.remove("generation")
  }

final case class PackageState(
    inputs: Vector[InspectedPackage],
    remaining: Int,
    sources: Vector[PackageSource]
)
object PackageState:
  val empty = PackageState(Vector.empty, 8, Vector.empty)
  given Codec.AsObject[PackageState] =
    Codec.forProduct3("inputs", "remaining", "sources")(PackageState.apply)(v =>
      (v.inputs, v.remaining, v.sources)
    )
