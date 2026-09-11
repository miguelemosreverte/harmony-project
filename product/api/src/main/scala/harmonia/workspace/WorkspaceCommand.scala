package harmonia.workspace

import harmonia.financing.FinancingAction
import harmonia.composition.CompositionCommand
import io.circe.Json

enum WorkspaceCommand:
  case Financing(action: FinancingAction)
  case Composition(command: CompositionCommand)

  def wire: String = this match
    case Financing(action)    => action.wire
    case Composition(command) => command.wire

  def parameters: Option[Json] = this match
    case Financing(_)         => None
    case Composition(command) => Some(command.parameters)

object WorkspaceCommand:
  /** Each feature owns its external command vocabulary. Routing remains exhaustive. */
  def read(action: String, parameters: Option[Json]): Either[String, WorkspaceCommand] =
    action match
      case "approve-financing" | "publish-approval" =>
        for
          _ <- Either.cond(parameters.isEmpty, (), "This action takes no extra input")
          action <- Json.fromString(action).as[FinancingAction].left.map(_.getMessage)
        yield Financing(action)
      case _ => CompositionCommand.read(action, parameters).map(Composition(_))
