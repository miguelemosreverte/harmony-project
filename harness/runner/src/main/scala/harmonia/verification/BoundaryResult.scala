package harmonia.verification

import cats.syntax.all.*
import io.circe.Json

object BoundaryResult:
  private val coreNumbers = Set(
    "maximum_steps",
    "approved_sources",
    "maximum_join_prerequisites",
    "maximum_composer_steps",
    "composition_approved_sources",
    "maximum_proposals"
  )
  private val coreFlags = Set(
    "join_complete",
    "graph_overflow_rejected",
    "stale_contract_rejected",
    "request_collision_rejected",
    "duplicate_kept_identity",
    "missing_disclosure_rejected",
    "composition_overflow_rejected",
    "proposal_overflow_rejected"
  )
  private val raceNumbers = Set(
    "committed",
    "conflicting",
    "approved_sources",
    "active_processes",
    "completed_steps",
    "committed_transactions"
  )
  def read(json: Json): Either[String, Json] = for
    _ <- Either.cond(
      json.asObject.exists(_.keys.toSet == Set("core", "race")),
      (),
      "Boundary result requires core and race observations"
    )
    _ <- section(json, "core", coreNumbers, coreFlags)
    _ <- section(json, "race", raceNumbers, Set("original_source_archived"))
  yield json

  private def section(
      json: Json,
      key: String,
      numbers: Set[String],
      flags: Set[String]
  ): Either[String, Unit] = for
    value <- json.hcursor.get[Json](key).leftMap(_ => s"Missing $key observations")
    _ <- Either.cond(
      value.asObject.exists(_.keys.toSet == numbers ++ flags),
      (),
      s"Unexpected $key observation fields"
    )
    _ <- numbers.toVector.traverse_(name =>
      value.hcursor
        .get[Int](name)
        .leftMap(_ => s"$name must be an integer")
        .flatMap(n => Either.cond(n >= 0 && n <= 512, (), s"$name exceeds the observation bound"))
    )
    _ <- flags.toVector.traverse_(name =>
      value.hcursor.get[Boolean](name).leftMap(_ => s"$name must be boolean").void
    )
  yield ()
