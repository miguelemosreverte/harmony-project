package harmonia.live

import harmonia.ui.Elements.*
import org.scalajs.dom
import scala.util.Try

/** A shared workspace URL identifies a task; a private participant link grants access. */
private[live] object SessionEntry:
  def render(expired: Boolean): Unit =
    val main = element("main", "live-main session-entry"); main.id = "main"
    val header = element("header", "reference-header")
    val brand = link("Harmonia", "/book/"); brand.className = "reference-brand"
    append(header, brand, link("Explore the book →", "/book/"))
    val content = element("section", "session-entry-content")
    append(
      content,
      element("p", "eyebrow", "Live sandbox"),
      element(
        "h1",
        text = if expired then "Open a current participant link." else "Enter as a participant."
      ),
      element(
        "p",
        text =
          if expired then
            "This participant link is no longer accepted. Open a link from the running sandbox’s launcher to continue."
          else
            "This address selects a workspace. To act in it, open Bank, Buyer or Reviewer from the sandbox’s local open.html launcher."
      )
    )
    val roles = element("ol", "session-roles")
    Vector(
      "Bank" -> "Approve the private financing case.",
      "Buyer" -> "Use the approval to continue.",
      "Reviewer" -> "Observe the shared result."
    ).foreach { (name, purpose) =>
      val role = element("li")
      append(role, element("strong", text = name), element("p", text = purpose))
      append(roles, role)
    }
    val form = element("form").asInstanceOf[dom.html.Form]
    val label = element("label", text = "Have a participant link? Paste it here.")
    label.setAttribute("for", "participant-link")
    val input = element("input").asInstanceOf[dom.html.Input]
    input.id = "participant-link"; input.`type` = "url"; input.required = true
    input.autocomplete = "off"; input.placeholder = "Private link from the sandbox launcher"
    input.setAttribute("spellcheck", "false")
    input.setAttribute("aria-describedby", "participant-link-error")
    val error = element("p"); error.id = "participant-link-error";
    error.setAttribute("role", "alert")
    val submit = element("button", "primary", "Enter workspace →").asInstanceOf[dom.html.Button]
    submit.`type` = "submit"
    form.onsubmit = event =>
      event.preventDefault()
      val destination = Try(new dom.URL(input.value.trim)).toOption.filter(url =>
        url.origin == dom.window.location.origin && url.pathname == "/" &&
          url.hash.matches("#session=[A-Za-z0-9_-]{43}")
      )
      destination match
        case Some(url) => dom.window.location.hash = url.hash
        case None =>
          error.textContent =
            "Use a participant link for this running sandbox, copied from its local launcher."
          input.setAttribute("aria-invalid", "true"); input.focus()
    append(form, label, input, error, submit)
    val recorded = link(
      "Explore the recorded handoff →",
      "/book/source/design/0.2/laboratory.html?story=live-handoff"
    )
    recorded.id = "session-recorded-handoff"
    append(
      content,
      roles,
      form,
      element(
        "p",
        "session-hint",
        "Keep each participant in its own tab. Shared workspace addresses do not include participant access."
      ),
      recorded
    )
    append(main, header, content)
    val root = dom.document.getElementById("app"); root.textContent = ""; append(root, main)
