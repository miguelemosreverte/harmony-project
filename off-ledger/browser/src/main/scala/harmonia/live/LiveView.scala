package harmonia.live

import harmonia.book.Elements.*
import io.circe.Json
import harmonia.financing.*
import harmonia.protocol.SubmissionStatus
import harmonia.workspace.WorkspaceSnapshot
import org.scalajs.dom

object LiveView:
  private var previous = ""
  def render(
      snapshot: Option[WorkspaceSnapshot],
      connection: String,
      unconfirmed: Boolean,
      submitting: Boolean,
      notice: Option[String],
      editor: harmonia.composition.CompositionEditor,
      packages: harmonia.builder.PackagePanel,
      reconnect: () => Unit,
      retry: () => Unit,
      dismiss: () => Unit,
      submit: String => Unit,
      compose: (String, Json) => Unit
  ): Unit =
    val signature = snapshot
      .map(_.toString)
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
        case Some(observation) =>
          val financing = observation.financing
          val actor = financing.actor
          val workflow = financing.progress
          val proof = financing.evidenceAvailable
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
              if index == 2 then workflow == ProgressStatus.Complete
              else proof || workflow == ProgressStatus.Complete
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
            element("p", text = financing.currentStep)
          )
          val status = element("p", "live-workflow", s"Workflow: ${workflow.wire}");
          status.id = "live-workflow"
          append(state, status)
          financing.application.foreach { application =>
            append(
              state,
              element("h3", text = "Your private application"),
              element("p", text = s"Status: ${application.wire}"),
              element("p", text = financing.privateDetails.getOrElse(""))
            )
          }
          val actions = financing.eligible
          val pending = observation.submissions.exists(_.outcome == SubmissionStatus.Pending)
          actions.foreach { action =>
            val label =
              if action == FinancingAction.Approve then "Approve financing"
              else "Continue shared workflow"
            val control = button(label, "primary", "live-" + action.wire)(submit(action.wire))
            control.disabled = pending || unconfirmed || submitting || connection != "Connected"
            append(state, control)
          }
          if actions.isEmpty then
            append(
              state,
              element(
                "p",
                text =
                  if workflow == ProgressStatus.Complete then "This handoff is complete."
                  else "No action is currently available for your session."
              )
            )
          val jobs = element("section", "live-panel")
          append(jobs, element("h2", text = "Your submissions"))
          val submissions = observation.submissions
          if submissions.isEmpty then
            append(jobs, element("p", text = "No commands submitted in this session."))
          submissions.reverse.foreach { job =>
            val row = element("article", "live-job")
            append(
              row,
              element("strong", text = job.outcome.wire),
              element("p", text = job.action),
              element("p", text = job.detail)
            )
            append(jobs, row)
          }
          append(grid, state, jobs); append(main, grid)
          Some(observation.composition).foreach { composition =>
            append(
              main,
              harmonia.composition.ComposerView.render(
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
          observation.history.foreach { transaction =>
            val row = element("li")
            append(
              row,
              element(
                "p",
                text = transaction.events.mkString(" → ")
              )
            )
            val details = element("details")
            append(
              details,
              element("summary", text = "Transaction identity"),
              element("code", text = transaction.updateId.getOrElse(""))
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
