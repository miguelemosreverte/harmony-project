package harmonia.book.diagram

import harmonia.book.RecordedStory
import harmonia.examples.ExampleKind
import harmonia.scene.*
import io.circe.Json

/** Recorded facts choose the diagram. Expected values belong only to the comparison. */
object StoryDiagram:
  def apply(story: RecordedStory, index: Int): WorkflowDiagram =
    val unit = story.units(index)
    val actual = unit.actual
    def text(value: Json, key: String): String =
      value.hcursor.get[String](key).getOrElse("Not observed")
    def words(key: String): Set[String] = actual.hcursor.get[Set[String]](key).getOrElse(Set.empty)
    def count(key: String): Option[Int] = actual.hcursor.get[Int](key).toOption
    val application =
      story.input.hcursor.downField("setup").downField("application").focus.getOrElse(Json.Null)
    val bank = text(application, "bank")
    val buyer = text(application, "buyer")
    def status(id: String): DiagramState =
      if words("completed")(id) then DiagramState.Complete
      else if words("skipped")(id) then DiagramState.Skipped
      else if words("enabled")(id) then DiagramState.Current
      else DiagramState.Pending
    def observed(id: String, label: String, actor: String, field: String): DiagramNode =
      DiagramNode(id, label, text(actual, field), actor, status(id))
    val (nodes, links) = story.kind match
      case ExampleKind.Progression =>
        val financing = observed("financing", "Approve financing", bank, "application")
        val review = observed("review", "Confirm the review", buyer, "review")
        if story.input.hcursor.get[String]("workflow").contains("branching-approval") then
          Vector(
            observed("decision", "Choose a route", bank, "branch"),
            financing,
            review,
            observed("closure", "Record the decline", bank, "closure"),
            observed("join", "Complete the selected path", buyer, "workflow")
          ) -> Vector(
            "decision" -> "financing",
            "decision" -> "review",
            "decision" -> "closure",
            "financing" -> "join",
            "review" -> "join",
            "closure" -> "join"
          )
        else Vector(financing, review) -> Vector("financing" -> "review")
      case ExampleKind.Composition =>
        val started = count("instances").exists(_ > 0)
        val proposed = count("drafts").exists(_ > 0)
        val steps = story.input.hcursor
          .downField("plan")
          .get[Vector[Json]]("steps")
          .getOrElse(Vector.empty)
          .map { step =>
            val id = text(step, "id")
            val source =
              actual.hcursor.downField("sources").get[String](id).getOrElse("No source observed")
            DiagramNode(
              "action-" + id,
              id,
              source,
              text(step, "actor") + " · " + text(step, "role"),
              status(id)
            )
          }
        val stages = Vector(
          DiagramNode(
            "proposal",
            "Propose the plan",
            "The bank proposes these exact actions.",
            "Bank",
            if started || proposed then DiagramState.Complete else DiagramState.Pending
          ),
          DiagramNode(
            "consent",
            "Accept the plan",
            "Sources are created only after partner consent.",
            "Buyer",
            if started then DiagramState.Complete
            else if proposed then DiagramState.Current
            else DiagramState.Pending
          )
        ) ++ steps
        stages -> stages.zip(stages.drop(1)).map((a, b) => a.id -> b.id)
      case ExampleKind.Purchase =>
        val offer =
          story.input.hcursor.downField("setup").downField("offer").focus.getOrElse(Json.Null)
        Vector(
          observed("assessment", "Assess financing", bank, "application"),
          observed("proposal", "Create the proposal", buyer, "proposal"),
          observed("relay", "Relay the proposal", text(offer, "buyer_agent"), "proposal"),
          observed("receipt", "Receive the proposal", text(offer, "seller_agent"), "workflow")
        ) -> Vector("assessment" -> "proposal", "proposal" -> "relay", "relay" -> "receipt")
      case ExampleKind.Transfer =>
        val trade =
          story.input.hcursor.downField("setup").downField("trade").focus.getOrElse(Json.Null)
        val settled = actual.hcursor.get[String]("trade").contains("settled")
        val source = actual.hcursor.downField("source").focus.getOrElse(Json.Null)
        val ready = actual.hcursor.get[String]("trade").contains("ready") || settled
        val locked =
          source.hcursor.get[String]("locked").exists(v => v.toDoubleOption.exists(_ > 0))
        Vector(
          DiagramNode(
            "source",
            "Reserve at source",
            "Available: " + text(source, "available") + "; locked: " + text(source, "locked"),
            text(trade, "source"),
            if locked || settled then DiagramState.Complete else DiagramState.Pending
          ),
          DiagramNode(
            "destination",
            "Prepare receipt",
            "Destination received: " + text(actual, "destination"),
            text(trade, "destination"),
            if ready then DiagramState.Complete else DiagramState.Pending
          ),
          DiagramNode(
            "settle",
            "Settle in one transaction",
            text(actual, "trade"),
            text(trade, "settler"),
            if settled then DiagramState.Complete
            else if unit.action == "settle" && unit.outcomeLabel == "rejected" then
              DiagramState.Refused
            else if ready then DiagramState.Current
            else DiagramState.Pending
          )
        ) -> Vector("source" -> "settle", "destination" -> "settle")
      case ExampleKind.Packages | ExampleKind.Boundaries =>
        val response =
          if story.kind == ExampleKind.Packages then
            actual.hcursor.get[Int]("http").toOption.fold("No HTTP observation")(n => s"HTTP $n")
          else unit.outcomeLabel
        val refused = actual.hcursor.get[Int]("http").exists(_ >= 400)
        Vector(
          DiagramNode(
            "actor",
            unit.actor,
            "Actor or verification owner named in the recording.",
            "Recorded actor",
            DiagramState.Pending
          ),
          DiagramNode(
            "action",
            unit.action.replace('-', ' '),
            unit.id,
            "Recorded operation",
            DiagramState.Current
          ),
          DiagramNode(
            "observation",
            response,
            unit.observedState,
            "Observed response",
            if refused then DiagramState.Refused
            else if actual.hcursor
                .get[Int]("http")
                .isRight || (story.kind == ExampleKind.Boundaries && actual.asObject
                .exists(_.nonEmpty))
            then DiagramState.Complete
            else DiagramState.Pending
          )
        ) -> Vector("actor" -> "action", "action" -> "observation")
      case ExampleKind.Financing | ExampleKind.Workflow | ExampleKind.Adapter |
          ExampleKind.Generated | ExampleKind.Private =>
        val state = text(actual, "application")
        val issued = state == "approved"
        val source = DiagramNode(
          "application",
          if state == "Not observed" then "Application not disclosed" else "Application: " + state,
          "The application retains its choice authority.",
          bank,
          if issued then DiagramState.Complete else DiagramState.Pending
        )
        val result = DiagramNode(
          "result",
          "Scoped application result",
          "Evidence available: " + actual.hcursor
            .get[Boolean]("evidence_available")
            .toOption
            .fold("Not observed")(_.toString),
          "Application boundary",
          if actual.hcursor.get[Boolean]("evidence_available").contains(true) then
            DiagramState.Complete
          else DiagramState.Pending
        )
        val shared = DiagramNode(
          "workflow",
          "Shared progress",
          text(actual, "workflow"),
          buyer,
          if actual.hcursor.get[String]("workflow").contains("complete") then DiagramState.Complete
          else DiagramState.Pending
        )
        if story.kind == ExampleKind.Financing then
          Vector(
            DiagramNode(
              "actor",
              unit.actor,
              unit.action.replace('-', ' '),
              "Attempting actor",
              if unit.outcomeLabel == "rejected" then DiagramState.Refused else DiagramState.Current
            ),
            source
          ) -> Vector("actor" -> "application")
        else if story.kind == ExampleKind.Private then
          Vector(source, result, shared) -> Vector(
            "application" -> "result",
            "result" -> "workflow"
          )
        else Vector(source, shared) -> Vector("application" -> "workflow")
    val states = nodes.map(n => n.id -> n.state).toMap
    WorkflowDiagram(
      unit.actor + " · " + unit.action.replace('-', ' '),
      unit.observedState,
      nodes,
      links.map((from, to) =>
        DiagramEdge(
          from,
          to,
          if states(from) == DiagramState.Skipped || states(to) == DiagramState.Skipped then
            DiagramState.Skipped
          else if states(from) == DiagramState.Refused then DiagramState.Refused
          else if states(to) == DiagramState.Complete then DiagramState.Complete
          else DiagramState.Pending
        )
      ),
      Some(SceneObservation(unit.actor, unit.id.replace('-', ' '), unit.outcomeLabel))
    )
