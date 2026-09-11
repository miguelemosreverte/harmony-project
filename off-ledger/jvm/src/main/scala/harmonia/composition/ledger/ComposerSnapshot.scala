package harmonia.composition.ledger

import harmonia.ledger.client.{ActiveContract, LedgerValue as V}
import io.circe.Json

object ComposerSnapshot:
  def json(contracts: Vector[ActiveContract], parties: Map[String, String], actor: String): Json =
    def identified(c: ActiveContract, module: String, entity: String) =
      c.template.getModuleName == module && c.template.getEntityName == entity
    def name(party: String): String = parties.find(_._2 == party).map(_._1).getOrElse("unknown")
    val workspace = contracts.find(c => identified(c, "Composer", "Workspace"))
    val references = workspace.toVector.flatMap(c =>
      V.fields(c).hcursor.get[Vector[String]]("references").getOrElse(Vector.empty)
    )
    val drafts = contracts.filter(c => identified(c, "Composer", "Draft")).map { draft =>
      val value = V.fields(draft)
      val steps = value.hcursor
        .get[Vector[Json]]("steps")
        .getOrElse(Vector.empty)
        .map(step =>
          step.mapObject(
            _.add("actor", Json.fromString(name(step.hcursor.get[String]("actor").toOption.get)))
          )
        )
      Json.obj(
        "name" -> Json.fromString(draft.text("name")),
        "reference" -> Json.fromString(draft.text("reference")),
        "steps" -> Json.arr(steps*),
        "can_accept" -> Json.fromBoolean(actor == "buyer"),
        "can_cancel" -> Json.fromBoolean(actor == "bank")
      )
    }
    val processes = contracts
      .filter(c =>
        identified(c, "Harmonia.Process.Engine", "ProcessInstance") && references.contains(
          c.text("reference")
        )
      )
      .map { process =>
        val value = V.fields(process).hcursor
        val definition = value.downField("definition")
        val completed = value.get[Vector[String]]("completed").getOrElse(Vector.empty)
        val roles = value
          .get[Vector[Json]]("roles")
          .getOrElse(Vector.empty)
          .map(role =>
            role.hcursor
              .get[String]("name")
              .toOption
              .get -> role.hcursor.get[String]("party").toOption.get
          )
          .toMap
        val bindings = value
          .get[Vector[Json]]("bindings")
          .getOrElse(Vector.empty)
          .map(binding =>
            binding.hcursor
              .get[String]("step")
              .toOption
              .get -> binding.hcursor.get[String]("action").toOption.get
          )
          .toMap
        val steps = definition.get[Vector[Json]]("steps").getOrElse(Vector.empty).map { spec =>
          val step = spec.hcursor
          val id = step.get[String]("id").toOption.get
          val role = step.get[String]("role").toOption.get
          val owner = name(roles(role))
          val source = bindings.get(id).flatMap(cid => contracts.find(_.id == cid))
          val application = source.flatMap { c =>
            if c.template.getModuleName == "GeneratedFinancing" then
              V.fields(c)
                .hcursor
                .get[String]("source")
                .toOption
                .flatMap(cid => contracts.find(_.id == cid))
            else Some(c)
          }
          val enabled = !completed.contains(id) && step
            .get[Vector[String]]("prerequisites")
            .getOrElse(Vector.empty)
            .forall(completed.contains)
          Json.obj(
            "id" -> Json.fromString(id),
            "role" -> Json.fromString(role),
            "actor" -> Json.fromString(owner),
            "completed" -> Json.fromBoolean(completed.contains(id)),
            "enabled" -> Json.fromBoolean(enabled),
            "can_execute" -> Json.fromBoolean(enabled && owner == actor),
            "source" -> application.fold(Json.Null)(c =>
              Json.fromString(c.template.getModuleName + "." + c.template.getEntityName)
            ),
            "status" -> application.fold(Json.Null)(c => Json.fromString(c.text("status"))),
            "integration" -> Json.fromString(
              if source.exists(_.template.getModuleName == "GeneratedFinancing") then "generated"
              else "direct"
            )
          )
        }
        Json.obj(
          "name" -> definition.get[String]("name").toOption.fold(Json.Null)(Json.fromString),
          "reference" -> Json.fromString(process.text("reference")),
          "complete" -> Json.fromBoolean(
            steps.nonEmpty && steps.forall(_.hcursor.get[Boolean]("completed").contains(true))
          ),
          "steps" -> Json.arr(steps*)
        )
      }
    Json.obj(
      "available" -> Json.fromBoolean(workspace.nonEmpty),
      "can_propose" -> Json.fromBoolean(
        actor == "bank" && workspace.nonEmpty && references.size < 8
      ),
      "remaining_proposals" -> Json.fromInt(math.max(0, 8 - references.size)),
      "drafts" -> Json.arr(drafts*),
      "processes" -> Json.arr(processes*)
    )
