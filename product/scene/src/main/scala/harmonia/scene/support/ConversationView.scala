package harmonia.scene.support

import org.scalajs.dom

/** Retained speakers and speech bubbles preserve layout while a workflow advances. */
final class ConversationView(parent: dom.HTMLElement):
  private val root = node("div", "workflow-support")
  root.setAttribute("role", "group")
  root.setAttribute("aria-label", "The moment explained")
  private val exchange = node("div", "support-conversation")
  root.appendChild(exchange)
  private val speakers = Vector.fill(2) {
    val person = node("div", "support-speaker")
    val portrait = node("div", "support-portrait")
    portrait.setAttribute("aria-hidden", "true")
    val initial = node("span", "support-initial")
    portrait.appendChild(initial)
    val speech = node("p", "support-speech")
    speech.setAttribute("aria-live", "polite")
    person.appendChild(portrait); person.appendChild(speech); exchange.appendChild(person)
    (portrait, initial, speech)
  }
  parent.appendChild(root)
  hide(root, true)

  def render(value: Option[Conversation]): Unit =
    hide(root, value.isEmpty)
    value.foreach { conversation =>
      Vector(conversation.first, conversation.second).zip(speakers).foreach {
        case (line, (portrait, initial, speech)) =>
          portrait.setAttribute("data-portrait", line.portrait.toString.toLowerCase)
          hide(initial, line.portrait != Portrait.Bank)
          initial.textContent = line.name.take(1).toUpperCase
          speech.setAttribute("aria-label", line.name + ": " + line.text)
          if speech.textContent != line.text then speech.textContent = line.text
      }
    }

  private def hide(element: dom.HTMLElement, hidden: Boolean): Unit =
    if hidden then element.setAttribute("hidden", "") else element.removeAttribute("hidden")

  private def node(tag: String, css: String): dom.HTMLElement =
    val result = dom.document.createElement(tag).asInstanceOf[dom.HTMLElement]
    result.className = css
    result
