package harmonia.composition.model

import io.circe.{Json, Decoder, Encoder}

enum CompositionActor(val wire: String):
  case Bank extends CompositionActor("bank")
  case Buyer extends CompositionActor("buyer")
object CompositionActor:
  given Decoder[CompositionActor] = Decoder.decodeString.emap(s =>
    CompositionActor.values.find(_.wire == s).toRight("Bind each role to the bank or buyer session")
  )

enum CompositionAction(val wire: String, val label: String):
  case Approve extends CompositionAction("approve-financing", "Approve financing · direct")
  case Review extends CompositionAction("confirm-review", "Confirm review · direct")
  case GeneratedApproval
      extends CompositionAction("approve-generated", "Approve financing · generated adapter")
object CompositionAction:
  given Decoder[CompositionAction] = Decoder.decodeString.emap(s =>
    CompositionAction.values
      .find(_.wire == s)
      .toRight("Supported actions: direct approval, generated approval, and review")
  )

final case class PlannedStep(
    id: String,
    role: String,
    actor: CompositionActor,
    action: CompositionAction
):
  def json: Json = Json.obj(
    "id" -> Json.fromString(id),
    "role" -> Json.fromString(role),
    "actor" -> Json.fromString(actor.wire),
    "action" -> Json.fromString(action.wire)
  )
object PlannedStep:
  given Encoder[PlannedStep] = Encoder.instance(_.json)
  given Decoder[PlannedStep] =
    Decoder.forProduct4("id", "role", "actor", "action")(PlannedStep.apply)
final case class Composition(name: String, reference: String, steps: Vector[PlannedStep]):
  def json: Json = Json.obj(
    "name" -> Json.fromString(name),
    "reference" -> Json.fromString(reference),
    "steps" -> Json.arr(steps.map(_.json)*)
  )

object Composition:
  def read(json: Json): Either[String, Composition] = for
    _ <- fields(json, Set("name", "reference", "steps"))
    name <- string(json, "name", 80)
    reference <- string(json, "reference", 80)
    values <- json.hcursor.get[Vector[Json]]("steps").left.map(_ => "Steps must be a list")
    _ <- Either.cond(values.nonEmpty && values.size <= 4, (), "Choose one to four ordered actions")
    steps <- values.foldLeft[Either[String, Vector[PlannedStep]]](Right(Vector.empty)) {
      (acc, step) =>
        for
          previous <- acc
          _ <- fields(step, Set("id", "role", "actor", "action"))
          id <- string(step, "id", 40)
          role <- string(step, "role", 40)
          actor <- step.hcursor.get[CompositionActor]("actor").left.map(_.getMessage)
          action <- step.hcursor.get[CompositionAction]("action").left.map(_.getMessage)
        yield previous :+ PlannedStep(id, role, actor, action)
    }
    _ <- Either.cond(steps.map(_.id).distinct.size == steps.size, (), "Step names must be distinct")
    _ <- Either.cond(
      steps.groupBy(_.role).values.forall(_.map(_.actor).distinct.size == 1),
      (),
      "A named role must always bind to the same party"
    )
  yield Composition(name, reference, steps)

  def fields(json: Json, names: Set[String]): Either[String, Unit] = Either.cond(
    json.asObject.exists(_.keys.toSet == names),
    (),
    s"Expected fields: ${names.toVector.sorted.mkString(", ")}"
  )
  def string(json: Json, name: String, limit: Int): Either[String, String] = json.hcursor
    .get[String](name)
    .left
    .map(_ => s"$name must be text")
    .flatMap(value =>
      Either.cond(
        value.trim.nonEmpty && value.length <= limit,
        value,
        s"$name must contain 1–$limit characters"
      )
    )
