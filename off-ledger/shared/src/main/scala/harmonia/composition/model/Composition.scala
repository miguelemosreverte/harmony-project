package harmonia.composition.model

import io.circe.Json

final case class PlannedStep(id: String, role: String, actor: String, action: String)
final case class Composition(name: String, reference: String, steps: Vector[PlannedStep])

object Composition:
  val actions = Set("approve-financing", "confirm-review", "approve-generated")
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
          actor <- string(step, "actor", 20)
          action <- string(step, "action", 40)
          _ <- Either.cond(
            Set("bank", "buyer").contains(actor),
            (),
            "Bind each role to the bank or buyer session"
          )
          _ <- Either.cond(
            actions.contains(action),
            (),
            "Supported actions: direct approval, generated approval, and review"
          )
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
