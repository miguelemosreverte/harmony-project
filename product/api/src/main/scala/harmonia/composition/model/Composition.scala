package harmonia.composition.model

import cats.syntax.all.*
import harmonia.protocol.JsonCodec
import io.circe.{Codec, Json, Decoder, Encoder}

enum CompositionActor(val wire: String):
  case Bank extends CompositionActor("bank")
  case Buyer extends CompositionActor("buyer")
object CompositionActor:
  given Encoder[CompositionActor] = Encoder.encodeString.contramap(_.wire)
  given Decoder[CompositionActor] = Decoder.decodeString.emap(s =>
    CompositionActor.values.find(_.wire == s).toRight("Bind each role to the bank or buyer session")
  )

enum CompositionAction(val wire: String, val label: String):
  case Approve extends CompositionAction("approve-financing", "Approve financing · direct")
  case Review extends CompositionAction("confirm-review", "Confirm review · direct")
  case GeneratedApproval
      extends CompositionAction("approve-generated", "Approve financing · generated adapter")
object CompositionAction:
  given Encoder[CompositionAction] = Encoder.encodeString.contramap(_.wire)
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
)
object PlannedStep:
  given Codec.AsObject[PlannedStep] = JsonCodec.derived[PlannedStep]

final case class Composition(name: String, reference: String, steps: Vector[PlannedStep])
object Composition:
  given Codec.AsObject[Composition] = JsonCodec.derived[Composition]

  def read(json: Json): Either[String, Composition] = for
    _ <- fields(json, Set("name", "reference", "steps"))
    values <- json.hcursor.get[Vector[Json]]("steps").left.map(_ => "Steps must be a list")
    _ <- values.traverse_(step => fields(step, Set("id", "role", "actor", "action")))
    plan <- json.as[Composition].left.map(_.getMessage)
    valid <- validate(plan)
  yield valid

  /** The editor and HTTP boundary validate the same typed plan. */
  def validate(plan: Composition): Either[String, Composition] = for
    _ <- text(plan.name, "name", 80)
    _ <- text(plan.reference, "reference", 80)
    _ <- Either.cond(
      plan.steps.nonEmpty && plan.steps.size <= 4,
      (),
      "Choose one to four ordered actions"
    )
    _ <- plan.steps.traverse_(step => text(step.id, "id", 40) *> text(step.role, "role", 40))
    _ <- Either.cond(
      plan.steps.map(_.id).distinct.size == plan.steps.size,
      (),
      "Step names must be distinct"
    )
    _ <- Either.cond(
      plan.steps.groupBy(_.role).values.forall(_.map(_.actor).distinct.size == 1),
      (),
      "A named role must always bind to the same party"
    )
  yield plan

  def fields(json: Json, names: Set[String]): Either[String, Unit] = Either.cond(
    json.asObject.exists(_.keys.toSet == names),
    (),
    s"Expected fields: ${names.toVector.sorted.mkString(", ")}"
  )
  def string(json: Json, name: String, limit: Int): Either[String, String] =
    json.hcursor
      .get[String](name)
      .left
      .map(_ => s"$name must be text")
      .flatMap(value => text(value, name, limit))

  private def text(value: String, name: String, limit: Int): Either[String, String] =
    Either.cond(
      value.trim.nonEmpty && value.length <= limit,
      value,
      s"$name must contain 1–$limit characters"
    )
