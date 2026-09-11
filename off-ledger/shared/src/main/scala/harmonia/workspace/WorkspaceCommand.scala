package harmonia.workspace

import harmonia.financing.FinancingAction
import harmonia.composition.model.Composition
import io.circe.Json

enum WorkspaceCommand:
  case Financing(action: FinancingAction)
  case Propose(plan: Composition)
  case Accept(reference: String)
  case Cancel(reference: String)
  case Advance(reference: String, step: String)

  def wire: String = this match
    case Financing(action) => action.wire
    case Propose(_)        => "compose-propose"
    case Accept(_)         => "compose-accept"
    case Cancel(_)         => "compose-cancel"
    case Advance(_, _)     => "compose-advance"

object WorkspaceCommand:
  /** Text dispatch belongs at this external API boundary only. */
  def read(action: String, parameters: Option[Json]): Either[String, WorkspaceCommand] =
    def input = parameters.toRight("Composition input is required")
    def reference = for
      json <- input
      _ <- Composition.fields(json, Set("reference"))
      ref <- Composition.string(json, "reference", 80)
    yield ref
    action match
      case "approve-financing" | "publish-approval" =>
        for
          _ <- Either.cond(parameters.isEmpty, (), "This action takes no extra input")
          action <- Json.fromString(action).as[FinancingAction].left.map(_.getMessage)
        yield Financing(action)
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
