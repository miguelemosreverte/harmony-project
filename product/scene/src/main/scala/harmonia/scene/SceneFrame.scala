package harmonia.scene

import io.circe.{Codec, Decoder, Encoder}

enum SceneKind:
  case Purchase, Transfer, Financing

object SceneKind:
  given Encoder[SceneKind] = Encoder.encodeString.contramap(_.toString.toLowerCase)
  given Decoder[SceneKind] = Decoder.decodeString.emap(value =>
    SceneKind.values.find(_.toString.equalsIgnoreCase(value)).toRight("Unknown scene kind")
  )

final case class ScenePerson(id: String, name: String, role: String, icon: String)
object ScenePerson:
  given Codec.AsObject[ScenePerson] = Codec.AsObject.derived[ScenePerson]

/** Exact quantities are text. Fractions are only decorative; they never determine an outcome. */
final case class SceneAmount(label: String, value: String, fraction: Double)
object SceneAmount:
  given Codec.AsObject[SceneAmount] = Codec.AsObject.derived[SceneAmount]

/** A presentation value, supplied by a domain owner or a recorded-story projector. */
final case class SceneFrame(
    kind: SceneKind,
    title: String,
    caption: String,
    people: Vector[ScenePerson],
    phase: Int,
    focus: String,
    artifact: String,
    refused: Boolean,
    amounts: Vector[SceneAmount],
    observation: Option[SceneObservation] = None,
    approval: Option[DiagramState] = None
)
object SceneFrame:
  given Codec.AsObject[SceneFrame] = Codec.AsObject.derived[SceneFrame]
