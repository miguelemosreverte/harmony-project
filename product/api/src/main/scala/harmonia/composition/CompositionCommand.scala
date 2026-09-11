package harmonia.composition

import harmonia.composition.model.Composition
import io.circe.Json
import io.circe.syntax.*

enum CompositionCommand:
  case Propose(plan: Composition)
  case Accept(reference: String)
  case Cancel(reference: String)
  case Advance(reference: String, step: String)

  def wire: String = this match
    case Propose(_)    => "compose-propose"
    case Accept(_)     => "compose-accept"
    case Cancel(_)     => "compose-cancel"
    case Advance(_, _) => "compose-advance"

  def parameters: Json = this match
    case Propose(plan)     => plan.asJson
    case Accept(reference) => Json.obj("reference" -> Json.fromString(reference))
    case Cancel(reference) => Json.obj("reference" -> Json.fromString(reference))
    case Advance(reference, step) =>
      Json.obj("reference" -> Json.fromString(reference), "step" -> Json.fromString(step))

object CompositionCommand:
  def read(action: String, parameters: Option[Json]): Either[String, CompositionCommand] =
    def input = parameters.toRight("Composition input is required")
    def reference = for
      json <- input
      _ <- Composition.fields(json, Set("reference"))
      ref <- Composition.string(json, "reference", 80)
    yield ref
    action match
      case "compose-propose" => input.flatMap(Composition.read).map(Propose(_))
      case "compose-accept"  => reference.map(Accept(_))
      case "compose-cancel"  => reference.map(Cancel(_))
      case "compose-advance" =>
        for
          json <- input
          _ <- Composition.fields(json, Set("reference", "step"))
          ref <- Composition.string(json, "reference", 80)
          step <- Composition.string(json, "step", 40)
        yield Advance(ref, step)
      case _ => Left(s"Unsupported action: $action")
