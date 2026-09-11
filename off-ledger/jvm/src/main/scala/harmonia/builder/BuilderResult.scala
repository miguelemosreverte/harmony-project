package harmonia.builder

import cats.syntax.all.*
import io.circe.Json

object BuilderResult:
  def read(json: Json): Either[String, Json] = for
    _ <- fields(json, Set("builder", "actions", "download"))
    _ <- Either.cond(
      json.hcursor.get[String]("builder").contains("package-inputs"),
      (),
      "Unknown builder result"
    )
    actions <- json.hcursor
      .get[Vector[Json]]("actions")
      .leftMap(_ => "Builder actions must be a list")
    _ <- Either.cond(
      actions.nonEmpty && actions.size <= 16,
      (),
      "Use one to sixteen builder observations"
    )
    ids <- actions.traverse { row =>
      for
        status <- row.hcursor.get[Int]("http").leftMap(_ => "HTTP status must be an integer")
        _ <- Either.cond(Set(200, 400, 403).contains(status), (), "Unexpected builder HTTP outcome")
        _ <- fields(
          row,
          Set("id", "http", "inputs") ++ (if status == 200 then
                                            Set("supported", "available_live", "compiled")
                                          else Set.empty[String])
        )
        id <- row.hcursor.get[String]("id").leftMap(_ => "Observation needs an ID")
        _ <- Either.cond(id.matches("[a-z][a-z0-9-]{0,63}"), (), "Invalid builder observation ID")
        count <- row.hcursor.get[Int]("inputs").leftMap(_ => "Input count must be an integer")
        _ <- Either.cond(count >= 0 && count <= 8, (), "Input count exceeds the builder bound")
        _ <-
          if status != 200 then Right(())
          else
            Vector("supported", "available_live", "compiled").traverse_(key =>
              row.hcursor.get[Boolean](key).leftMap(_ => s"$key must be boolean").void
            )
      yield id
    }
    _ <- Either.cond(ids.distinct == ids, (), "Builder observation IDs must be unique")
    download <- json.hcursor.get[Json]("download").leftMap(_ => "Download evidence is required")
    _ <- fields(download, Set("source_unchanged", "compiled_artifacts_match"))
    _ <- Vector("source_unchanged", "compiled_artifacts_match").traverse_(key =>
      download.hcursor.get[Boolean](key).leftMap(_ => s"$key must be boolean").void
    )
  yield json

  private def fields(json: Json, names: Set[String]): Either[String, Unit] = Either.cond(
    json.asObject.exists(_.keys.toSet == names),
    (),
    s"Expected fields: ${names.toVector.sorted.mkString(", ")}"
  )
