package harmonia.live

import harmonia.ui.Elements.*
import harmonia.financing.FinancingPanel
import harmonia.protocol.SubmissionStatus
import harmonia.protocol.LedgerUpdate
import harmonia.workspace.{WorkspaceSnapshot, WorkspaceCommand, SubmissionView}
import org.scalajs.dom

/** Owns stable workspace regions. Polling never detaches the draft editor or package controls. */
final class LiveView:
  private val main = element("main", "live-main"); main.id = "main"
  private val feedback = element("section")
  private val identity = element("p", "live-identity"); identity.id = "live-identity"
  private val finance = element("div")
  private val jobs = element("section", "live-panel")
  private val composition = element("section", "live-panel"); composition.id = "composer"
  private val draft = element("div")
  private val composed = element("div")
  private val packageNavigation = link("Application packages", "#package-builder")
  private val packageArea = element("div")
  private val history = element("details", "live-panel")
  private val historyBody = element("div")
  private var previous: Option[WorkspaceSnapshot] = None
  private var previousBlocked = true
  private var previousFeedback: Option[(ConnectionState, Boolean, Boolean, Option[String])] = None
  private var mounted = false

  def render(
      snapshot: Option[WorkspaceSnapshot],
      connection: ConnectionState,
      unconfirmed: Boolean,
      submitting: Boolean,
      notice: Option[String],
      editor: harmonia.composition.CompositionEditor,
      packages: harmonia.packages.PackagePanel,
      reconnect: () => Unit,
      retry: () => Unit,
      dismiss: () => Unit,
      submit: WorkspaceCommand => Unit
  ): Unit =
    if !mounted then
      mounted = true
      append(
        main,
        element("p", "eyebrow", "HARMONIA / LIVE PARTICIPANT SESSION"),
        element("h1", text = "A private decision. A shared next step."),
        element(
          "p",
          "lede",
          "The bank approves its private financing case. The buyer uses the signed result to continue. Each session sees what its own participant discloses."
        ),
        feedback,
        identity
      )
      val nav = element("nav", "workspace-nav"); nav.setAttribute("aria-label", "Workspace tasks")
      Vector(
        "Financing" -> "financing",
        "Compose a workflow" -> "composer"
      ).foreach((title, id) => append(nav, link(title, "#" + id)))
      append(nav, packageNavigation)
      finance.id = "financing"
      append(main, nav, finance, jobs)
      append(
        composition,
        element("h2", text = "Build a workflow together"),
        element(
          "p",
          text =
            "Propose → partner consent → execute → inspect. These evaluation sources are shared with both parties; the private handoff above keeps its own disclosure rules."
        ),
        draft,
        composed
      )
      append(
        history,
        element("summary", text = "Visible ledger history"),
        element(
          "p",
          text =
            "These events came from your authenticated participant query. Refreshing recovers committed state."
        ),
        historyBody
      )
      append(
        main,
        composition,
        packageArea,
        history,
        element(
          "p",
          "live-footnote",
          "Local evaluation · synthetic data · one synchronizer · state lasts until the local network stops. Recorded playback is available separately in the book."
        )
      )
      val root = dom.document.getElementById("app"); root.textContent = ""; append(root, main)
    val feedbackState = (connection, unconfirmed, submitting, notice)
    if !previousFeedback.contains(feedbackState) then
      previousFeedback = Some(feedbackState)
      feedback.textContent = ""
      val message =
        if submitting then connection.label + " · Pending — waiting for the server"
        else if unconfirmed then
          connection.label + " · Previous submission is unconfirmed. Reconnect or retry the same request."
        else connection.label
      val banner = element("p", "live-connection", message); banner.id = "live-connection";
      banner.setAttribute("role", "status")
      append(
        feedback,
        banner,
        button("Reconnect / refresh", "secondary", "live-refresh")(reconnect())
      )
      if unconfirmed && !submitting && connection == ConnectionState.Connected then
        append(feedback, button("Retry unconfirmed request", "secondary", "live-retry")(retry()))
      notice.foreach { message =>
        val diagnostic = element("div", "live-diagnostic"); diagnostic.id = "live-diagnostic";
        diagnostic.setAttribute("role", "alert")
        append(
          diagnostic,
          element("p", text = message),
          button("Dismiss", "secondary", "live-dismiss")(dismiss())
        )
        append(feedback, diagnostic)
      }
    snapshot match
      case None =>
        identity.textContent = "Open the participant link supplied by the local operator."
      case Some(state) =>
        val actor = state.financing.actor
        identity.textContent = "Authenticated as " + Map(
          "bank" -> "Bank",
          "buyer" -> "Buyer",
          "reviewer" -> "Olivia · observer"
        ).getOrElse(actor, actor)
        val blocked = state.submissions.exists(
          _.outcome == SubmissionStatus.Pending
        ) || unconfirmed || submitting || connection != ConnectionState.Connected
        if previous.map(_.financing) != Some(state.financing) || blocked != previousBlocked then
          replace(
            finance,
            FinancingPanel.render(
              state.financing,
              blocked,
              a => submit(WorkspaceCommand.Financing(a))
            )
          )
        if previous.map(_.submissions) != Some(state.submissions) then renderJobs(state.submissions)
        val compositionState = state.composition
        draft.style.display = if compositionState.canPropose then "" else "none"
        val input = editor.render(blocked, compositionState.remainingProposals)
        if input.parentNode != draft then append(draft, input)
        if previous.map(_.composition) != Some(compositionState) || blocked != previousBlocked then
          replace(
            composed,
            harmonia.composition.ComposerView.render(compositionState, blocked, submit)
          )
        packageNavigation.style.display = if actor == "bank" then "" else "none"
        packageArea.style.display = if actor == "bank" then "" else "none"
        if actor == "bank" then
          val panel = packages.render()
          if panel.parentNode != packageArea then append(packageArea, panel)
        if previous.map(_.history) != Some(state.history) then renderHistory(state.history)
        previous = Some(state); previousBlocked = blocked

  private def replace(parent: dom.HTMLElement, child: dom.HTMLElement): Unit =
    val focused =
      Option(dom.document.activeElement).filter(parent.contains).map(_.id).filter(_.nonEmpty)
    parent.textContent = ""; append(parent, child)
    focused.foreach { id =>
      Option(dom.document.getElementById(id)).filterNot(_.hasAttribute("disabled")) match
        case Some(node) => node.asInstanceOf[dom.HTMLElement].focus()
        case None       => parent.tabIndex = -1; parent.focus()
    }

  private def renderJobs(values: Vector[SubmissionView]): Unit =
    jobs.textContent = ""; append(jobs, element("h2", text = "Your submissions"))
    if values.isEmpty then
      append(jobs, element("p", text = "No commands submitted in this session."))
    values.reverse.foreach { job =>
      val row = element("article", "live-job")
      append(
        row,
        element("strong", text = job.outcome.wire),
        element("p", text = job.action),
        element("p", text = job.detail)
      )
      append(jobs, row)
    }

  private def renderHistory(values: Vector[LedgerUpdate]): Unit =
    val list = element("ol", "live-history")
    values.foreach { tx =>
      val row = element("li")
      val details = element("details")
      append(
        details,
        element("summary", text = "Transaction identity"),
        element("code", text = tx.updateId.getOrElse(""))
      )
      append(row, element("p", text = tx.events.mkString(" → ")), details); append(list, row)
    }
    replace(historyBody, list)
