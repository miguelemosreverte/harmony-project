package harmonia.live

import harmonia.book.Elements.*
import io.circe.Json
import org.scalajs.dom

object LiveView:
  private var previous = ""
  def render(
      snapshot: Option[Json],
      connection: String,
      unconfirmed: Boolean,
      submitting: Boolean,
      notice: Option[String],
      editor: harmonia.composer.CompositionEditor,
      packages: harmonia.builder.PackagePanel,
      reconnect: () => Unit,
      retry: () => Unit,
      dismiss: () => Unit,
      submit: String => Unit,
      compose: (String, Json) => Unit
  ): Unit =
    val signature = snapshot
      .map(_.noSpaces)
      .getOrElse("") + connection + unconfirmed.toString + submitting.toString + notice.getOrElse(
      ""
    )
    if signature != previous then
      previous = signature
      val focused = Option(dom.document.activeElement).map(_.id).filter(_.nonEmpty)
      val root = dom.document.getElementById("app")
      root.textContent = ""
      val main = element("main", "live-main"); main.id = "main"
      val eyebrow = element("p", "eyebrow", "HARMONIA / LIVE PARTICIPANT SESSION")
      val title = element("h1", text = "A private decision. A shared next step.")
      val description = element(
        "p",
        "lede",
        "The bank approves its private financing case. The buyer uses the signed result to continue. Each session sees what its own participant discloses."
      )
      val feedback =
        if submitting then connection + " · Pending — waiting for the server"
        else if unconfirmed then
          connection + " · Previous submission is unconfirmed. Reconnect or retry the same request."
        else connection
      val banner = element("p", "live-connection", feedback);
      banner.setAttribute("role", "status"); banner.id = "live-connection"
      append(
        main,
        eyebrow,
        title,
        description,
        banner,
        button("Reconnect / refresh", "secondary", "live-refresh")(reconnect())
      )
      if unconfirmed && !submitting && connection == "Connected" then
        append(main, button("Retry unconfirmed request", "secondary", "live-retry")(retry()))
      notice.foreach { message =>
        val diagnostic = element("div", "live-diagnostic"); diagnostic.setAttribute("role", "alert")
        diagnostic.id = "live-diagnostic"
        append(
          diagnostic,
          element("p", text = message),
          button("Dismiss", "secondary", "live-dismiss")(dismiss())
        )
        append(main, diagnostic)
      }
      snapshot match
        case None =>
          append(
            main,
            element(
              "p",
              text =
                "Open the participant link supplied by the local operator. This page cannot select or grant a ledger identity."
            )
          )
        case Some(json) =>
          val cursor = json.hcursor
          val actor = cursor.get[String]("actor").getOrElse("")
          val workflow = cursor.get[String]("workflow").getOrElse("unknown")
          val proof = cursor.get[Boolean]("evidence_available").getOrElse(false)
          val actorLabel =
            Map("bank" -> "Bank", "buyer" -> "Buyer", "reviewer" -> "Olivia · observer")
              .getOrElse(actor, actor)
          val identity = element("p", "live-identity", s"Authenticated as $actorLabel");
          identity.id = "live-identity"
          append(main, identity)
          val graph = element("ol", "live-flow")
          val labels =
            Vector("Bank approves privately", "Bank issues a signed result", "Buyer continues")
          labels.zipWithIndex.foreach { (label, index) =>
            val done =
              if index == 2 then workflow == "complete" else proof || workflow == "complete"
            append(
              graph,
              element(
                "li",
                if done then "done" else "waiting",
                (if done then "✓ " else "○ ") + label
              )
            )
          }
          append(main, graph)
          val grid = element("div", "live-grid")
          val state = element("section", "live-panel")
          append(
            state,
            element("h2", text = "Current state"),
            element("p", text = cursor.get[String]("current_step").getOrElse("Waiting"))
          )
          val status = element("p", "live-workflow", s"Workflow: $workflow");
          status.id = "live-workflow"
          append(state, status)
          cursor.get[String]("application").toOption.foreach { application =>
            append(
              state,
              element("h3", text = "Your private application"),
              element("p", text = s"Status: $application"),
              element("p", text = cursor.get[String]("private_details").getOrElse(""))
            )
          }
          val actions = cursor.get[Vector[String]]("eligible").getOrElse(Vector.empty)
          val pending = cursor
            .get[Vector[Json]]("jobs")
            .getOrElse(Vector.empty)
            .exists(_.hcursor.get[String]("outcome").contains("pending"))
          actions.foreach { action =>
            val label =
              if action == "approve-financing" then "Approve financing"
              else "Continue shared workflow"
            val control = button(label, "primary", "live-" + action)(submit(action))
            control.disabled = pending || unconfirmed || submitting || connection != "Connected"
            append(state, control)
          }
          if actions.isEmpty then
            append(
              state,
              element(
                "p",
                text =
                  if workflow == "complete" then "This handoff is complete."
                  else "No action is currently available for your session."
              )
            )
          val jobs = element("section", "live-panel")
          append(jobs, element("h2", text = "Your submissions"))
          val submissions = cursor.get[Vector[Json]]("jobs").getOrElse(Vector.empty)
          if submissions.isEmpty then
            append(jobs, element("p", text = "No commands submitted in this session."))
          submissions.reverse.foreach { job =>
            val row = element("article", "live-job")
            append(
              row,
              element("strong", text = job.hcursor.get[String]("outcome").getOrElse("unknown")),
              element("p", text = job.hcursor.get[String]("action").getOrElse("")),
              element("p", text = job.hcursor.get[String]("detail").getOrElse(""))
            )
            append(jobs, row)
          }
          append(grid, state, jobs); append(main, grid)
          cursor.downField("composer").focus.foreach { composition =>
            append(
              main,
              harmonia.composer.ComposerView.render(
                composition,
                pending || unconfirmed || submitting || connection != "Connected",
                editor,
                compose
              )
            )
          }
          if actor == "bank" then append(main, packages.render())
          val history = element("section", "live-panel")
          append(
            history,
            element("h2", text = "Visible ledger history"),
            element(
              "p",
              text =
                "These events came from your authenticated participant query. Refreshing recovers committed state."
            )
          )
          val list = element("ol", "live-history")
          cursor.get[Vector[Json]]("history").getOrElse(Vector.empty).foreach { transaction =>
            val row = element("li")
            append(
              row,
              element(
                "p",
                text = transaction.hcursor
                  .get[Vector[String]]("events")
                  .getOrElse(Vector.empty)
                  .mkString(" → ")
              )
            )
            val details = element("details")
            append(
              details,
              element("summary", text = "Transaction identity"),
              element("code", text = transaction.hcursor.get[String]("update_id").getOrElse(""))
            )
            append(row, details); append(list, row)
          }
          append(history, list); append(main, history)
      append(
        main,
        element(
          "p",
          "live-footnote",
          "Local evaluation · synthetic data · one synchronizer · state lasts until the local network stops. Recorded playback is available separately in the book."
        )
      )
      root.appendChild(main)
      focused
        .flatMap(id => Option(dom.document.getElementById(id)))
        .foreach(_.asInstanceOf[dom.HTMLElement].focus())
