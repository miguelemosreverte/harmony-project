package harmonia.book.evidence

import io.circe.Json

object EvidenceValues:
  def text(json: Json, field: String): String =
    json.hcursor.get[String](field).getOrElse("Unknown")
  def display(value: Option[Json]): String = value match
    case None => "—"
    case Some(json) =>
      json.asString
        .orElse(json.asArray.map(_.map(v => v.asString.getOrElse(v.noSpaces)).mkString(", ")))
        .orElse(
          json.asObject.map(
            _.toVector
              .map((name, value) => s"${label(name)}: ${display(Some(value))}")
              .mkString("; ")
          )
        )
        .getOrElse(json.noSpaces)
  def label(field: String): String = Map(
    "available" -> "Available",
    "locked" -> "Locked",
    "trade" -> "Trade",
    "source" -> "Source balances",
    "destination" -> "Destination received",
    "active_workflows" -> "Active workflow contracts",
    "releases" -> "Unconsumed releases",
    "application" -> "Application",
    "review" -> "Review",
    "offer" -> "Current offer",
    "proposal" -> "Current proposal",
    "proposals" -> "Active proposals",
    "evidence_available" -> "Bound result active",
    "closure" -> "Closure",
    "branch" -> "Selected branch",
    "skipped" -> "Skipped steps",
    "completed" -> "Completed steps",
    "enabled" -> "Enabled steps",
    "workflow" -> "Workflow",
    "outcome" -> "Outcome",
    "reason" -> "Rejection reason",
    "consumed" -> "Previous contract consumed",
    "active_contracts" -> "Active applications",
    "visible_to" -> "Visible to"
  ).getOrElse(field, field)
