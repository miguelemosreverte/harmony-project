package harmonia.packages

import harmonia.protocol.JsonCodec
import io.circe.{Codec, Decoder, Encoder, Json}

final case class PackageSource(id: String, source: String)
object PackageSource:
  given Codec.AsObject[PackageSource] = JsonCodec.derived[PackageSource]

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
    packages: Map[String, Json],
    generation: Option[Json]
)
object InspectedPackage:
  private val fields = JsonCodec.derived[InspectedPackage]
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
  given Codec.AsObject[PackageState] = JsonCodec.derived[PackageState]
