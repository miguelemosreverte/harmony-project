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
  private val header = element("header", "reference-header")
  private val drawer = element("dialog", "reader-drawer").asInstanceOf[dom.HTMLDialogElement]
  private val feedback = element("section")
  private val identity = element("p", "live-identity"); identity.id = "live-identity"
  private val financing = new FinancingPanel
  private val finance = financing.root
  private val jobs = element("section", "live-panel")
  private val composition = element("section", "live-panel"); composition.id = "composer"
  private val draft = element("div")
  private val composed = element("div")
  private val packageNavigation = link("Applications", "?view=packages")
  private val packageArea = element("div")
  private val evidence = element("section")
  private val history = element("section", "live-panel")
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
      val brand = link("Harmonia", "/book/"); brand.className = "reference-brand"
      val tools = element("div", "reference-tools")
      val evidenceButton = button("Evidence", "reference-evidence", "open-workspace-evidence") {
        WorkspaceNavigation.navigate("evidence")
      }
      append(tools, evidenceButton)
      append(header, brand, tools)
      append(main, header, feedback)
      val drawerTitle = element("div", "drawer-heading")
      append(
        drawerTitle,
        element("h2", text = "Your workspace"),
        button("×", "", "close-workspace-evidence") { WorkspaceNavigation.navigate("financing") }
      )
      drawerTitle.querySelector("button").setAttribute("aria-label", "Close workspace")
      drawer.addEventListener(
        "cancel",
        (event: dom.Event) =>
          event.preventDefault(); WorkspaceNavigation.navigate("financing")
      )
      append(drawer, drawerTitle, identity)
      val nav = element("nav", "workspace-nav"); nav.setAttribute("aria-label", "Workspace tasks")
      Vector(
        "Financing" -> "financing",
        "Compose" -> "composer",
        "Evidence" -> "evidence"
      ).foreach((title, id) => append(nav, link(title, "?view=" + id)))
      append(nav, packageNavigation, link("The book ↗", "/book/"))
      finance.id = "financing"
      append(main, finance)
      append(drawer, nav)
      append(evidence, jobs, history)
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
        element("h2", text = "Visible ledger history"),
        element(
          "p",
          text =
            "These events came from your authenticated participant query. Refreshing recovers committed state."
        ),
        historyBody
      )
      append(
        drawer,
        composition,
        packageArea,
        evidence,
        element(
          "p",
          "live-footnote",
          "Disposable local sandbox · synthetic data"
        )
      )
      append(main, drawer)
      val root = dom.document.getElementById("app"); root.textContent = ""; append(root, main)
      new WorkspaceNavigation(nav, () => selectPage())
      val appearance = nav.querySelector(".workspace-appearance")
      tools.insertBefore(appearance, evidenceButton)
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
      append(feedback, banner)
      feedback.className =
        if connection == ConnectionState.Connected && !submitting && !unconfirmed && notice.isEmpty
        then "connection-summary"
        else "connection-attention"
      if connection != ConnectionState.Connected then
        append(feedback, button("Reconnect", "secondary", "live-refresh")(reconnect()))
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
          financing.render(state.financing, blocked, a => submit(WorkspaceCommand.Financing(a)))
        if previous.map(_.submissions) != Some(state.submissions) then renderJobs(state.submissions)
        val compositionState = state.composition
        hide(draft, !compositionState.canPropose)
        val input = editor.render(blocked, compositionState.remainingProposals)
        if input.parentNode != draft then append(draft, input)
        if previous.map(_.composition) != Some(compositionState) || blocked != previousBlocked then
          replace(
            composed,
            harmonia.composition.ComposerView.render(compositionState, blocked, submit)
          )
        hide(packageNavigation, actor != "bank")
        if actor == "bank" then
          val panel = packages.render()
          if panel.parentNode != packageArea then append(packageArea, panel)
        if previous.map(_.history) != Some(state.history) then renderHistory(state.history)
        previous = Some(state); previousBlocked = blocked
    selectPage()

  private def selectPage(): Unit =
    val page = WorkspaceNavigation.page
    hide(finance, false)
    if page != "financing" && !drawer.open then drawer.showModal()
    if page == "financing" && drawer.open then
      drawer.close()
      dom.document.getElementById("open-workspace-evidence").asInstanceOf[dom.HTMLElement].focus()
    hide(composition, page != "composer")
    hide(evidence, page != "evidence")
    hide(packageArea, page != "packages" || previous.forall(_.financing.actor != "bank"))
    if page == "packages" && previous.exists(_.financing.actor != "bank") then hide(finance, false)

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
