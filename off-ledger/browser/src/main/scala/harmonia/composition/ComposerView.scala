package harmonia.composition

import harmonia.book.Elements.*
import io.circe.Json
import org.scalajs.dom

object ComposerView:
  def render(
      state: Json,
      blocked: Boolean,
      editor: CompositionEditor,
      submit: (String, Json) => Unit
  ): dom.HTMLElement =
    val root = element("section", "live-panel"); root.id = "composer"
    append(
      root,
      element("h2", text = "Build a workflow together"),
      element(
        "p",
        text =
          "Propose → partner consent → execute → inspect. These evaluation source contracts are shared with both parties. The private handoff above keeps its separate disclosure rules."
      )
    )
    val cursor = state.hcursor
    if cursor.get[Boolean]("can_propose").contains(true) then
      append(root, editor.render(blocked, cursor.get[Int]("remaining_proposals").getOrElse(0)))
    else if !cursor.get[Boolean]("available").contains(true) then
      append(root, element("p", text = "Your session has no access to the composition workspace."))
    else
      append(
        root,
        element(
          "p",
          text = "The bank proposes; the buyer consents. Each action belongs to its assigned party."
        )
      )
    val drafts = cursor.get[Vector[Json]]("drafts").getOrElse(Vector.empty)
    drafts.zipWithIndex.foreach { (draft, index) =>
      val value = draft.hcursor
      val reference = value.get[String]("reference").getOrElse("")
      val card = element("article", "composition-record")
      append(
        card,
        element("h3", text = value.get[String]("name").getOrElse("Proposal")),
        element(
          "p",
          text = s"Reference: $reference · Awaiting buyer consent · Sources not yet created"
        )
      )
      val list = element("ol")
      value.get[Vector[Json]]("steps").getOrElse(Vector.empty).foreach { step =>
        append(
          list,
          element(
            "li",
            text = Vector("id", "role", "actor", "action")
              .map(k => step.hcursor.get[String](k).getOrElse(""))
              .mkString(" · ")
          )
        )
      }
      append(card, list)
      Vector(
        ("can_accept", "compose-accept", "Accept this plan"),
        ("can_cancel", "compose-cancel", "Cancel proposal")
      ).foreach { (allowed, action, label) =>
        if value.get[Boolean](allowed).contains(true) then
          val control = button(label, "primary", s"$action-$index")(
            submit(action, Json.obj("reference" -> Json.fromString(reference)))
          )
          control.disabled = blocked; append(card, control)
      }
      append(root, card)
    }
    val processes = cursor.get[Vector[Json]]("processes").getOrElse(Vector.empty)
    processes.zipWithIndex.foreach { (process, index) =>
      val value = process.hcursor
      val reference = value.get[String]("reference").getOrElse("")
      val complete = value.get[Boolean]("complete").contains(true)
      val card = element("article", "composition-record")
      append(
        card,
        element("h3", text = value.get[String]("name").getOrElse("Workflow")),
        element(
          "p",
          "composition-status",
          s"Reference: $reference · ${if complete then "Complete" else "In progress"}"
        )
      )
      val flow = element("ol", "live-flow")
      value.get[Vector[Json]]("steps").getOrElse(Vector.empty).zipWithIndex.foreach {
        (step, position) =>
          val s = step.hcursor
          val id = s.get[String]("id").getOrElse("")
          val done = s.get[Boolean]("completed").contains(true)
          val enabled = s.get[Boolean]("enabled").contains(true)
          val row = element("li", if done then "done" else "waiting")
          append(
            row,
            element(
              "strong",
              text = s"$id · ${if done then "Complete" else if enabled then "Ready" else "Waiting"}"
            ),
            element(
              "p",
              text =
                s"${s.get[String]("role").getOrElse("")} · ${s.get[String]("actor").getOrElse("")}"
            ),
            element(
              "p",
              text =
                s"${s.get[String]("source").getOrElse("Source unavailable")} · ${s.get[String]("status").getOrElse("unknown")}"
            ),
            element(
              "p",
              text = s"Integration: ${s.get[String]("integration").getOrElse("unknown")}"
            )
          )
          if s.get[Boolean]("can_execute").contains(true) then
            val control = button(s"Execute $id", "primary", s"compose-execute-$index-$position") {
              submit(
                "compose-advance",
                Json.obj("reference" -> Json.fromString(reference), "step" -> Json.fromString(id))
              )
            }
            control.disabled = blocked; append(row, control)
          append(flow, row)
      }
      append(card, flow); append(root, card)
    }
    if drafts.isEmpty && processes.isEmpty then
      append(root, element("p", text = "No workflow has been proposed yet."))
    root
