package harmonia.composer.ledger

import harmonia.composer.model.Composition
import harmonia.live.actions.ActionRequest
import harmonia.live.ledger.{ActiveContract, LedgerValue as V, LiveLedger}
import com.daml.ledger.api.v2.ValueOuterClass
import io.circe.Json

object ComposerCommands:
  val actions = Set("compose-propose", "compose-accept", "compose-cancel", "compose-advance")
  def validate(request: ActionRequest): Either[String, Unit] =
    request.parameters.toRight("Composition input is required").flatMap { input =>
      request.action match
        case "compose-propose" => Composition.read(input).map(_ => ())
        case "compose-accept" | "compose-cancel" =>
          for
            _ <- Composition.fields(input, Set("reference"))
            _ <- Composition.string(input, "reference", 80)
          yield ()
        case "compose-advance" =>
          for
            _ <- Composition.fields(input, Set("reference", "step"))
            _ <- Composition.string(input, "reference", 80)
            _ <- Composition.string(input, "step", 40)
          yield ()
        case _ => Left("Unsupported composition command")
    }

  def select(
      request: ActionRequest,
      contracts: Vector[ActiveContract],
      party: String,
      parties: Map[String, String]
  ): Option[(ActiveContract, String, ValueOuterClass.Value)] =
    val input = request.parameters.get
    def find(module: String, entity: String, reference: Option[String] = None) = contracts.find(c =>
      c.template.getModuleName == module && c.template.getEntityName == entity && reference.forall(
        _ == c.text("reference")
      )
    )
    request.action match
      case "compose-propose" =>
        val plan =
          Composition.read(input).fold(message => throw IllegalArgumentException(message), identity)
        val steps = plan.steps.map(step =>
          V.record(
            "id" -> V.text(step.id),
            "role" -> V.text(step.role),
            "actor" -> V.party(parties(step.actor)),
            "action" -> V.text(step.action)
          )
        )
        find("Composer", "Workspace").map(c =>
          (
            c,
            "Propose",
            V.record(
              "name" -> V.text(plan.name),
              "reference" -> V.text(plan.reference),
              "steps" -> V.list(steps)
            )
          )
        )
      case "compose-accept" | "compose-cancel" =>
        find("Composer", "Draft", input.hcursor.get[String]("reference").toOption).map(c =>
          (
            c,
            if request.action == "compose-accept" then "Accept" else "Cancel",
            LiveLedger.emptyArgument
          )
        )
      case "compose-advance" =>
        find(
          "Harmonia.Process.Engine",
          "ProcessInstance",
          input.hcursor.get[String]("reference").toOption
        ).map(c =>
          (
            c,
            "AdvanceStep",
            V.record(
              "step" -> V.text(input.hcursor.get[String]("step").toOption.get),
              "actor" -> V.party(party),
              "request" -> V.text(request.id)
            )
          )
        )
      case _ => None
