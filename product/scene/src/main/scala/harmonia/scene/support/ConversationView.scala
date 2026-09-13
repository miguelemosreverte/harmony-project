package harmonia.scene.support

import org.scalajs.dom

/** The illustration stays put; only the observed exchange changes as a step advances. */
final class ConversationView(parent: dom.HTMLElement):
  private val root = node("div", "workflow-support")
  root.setAttribute("role", "group")
  root.setAttribute("aria-label", "The moment explained")
  private val exchange = node("div", "support-conversation")
  private val art = node("div", "support-art")
  art.setAttribute("role", "img")
  private val dialogue = node("div", "support-dialogue")
  exchange.appendChild(art); exchange.appendChild(dialogue); root.appendChild(exchange)
  private val speakers = Vector.fill(2) {
    val person = node("div", "support-speaker")
    val name = node("span", "support-name")
    val speech = node("p", "support-speech")
    speech.setAttribute("aria-live", "polite")
    person.appendChild(name); person.appendChild(speech); dialogue.appendChild(person)
    (name, speech)
  }
  parent.appendChild(root)
  hide(root, true)

  def render(value: Option[Conversation]): Unit =
    hide(root, value.isEmpty)
    value.foreach { conversation =>
      hide(art, conversation.illustration.isEmpty)
      conversation.illustration.foreach { image =>
        art.setAttribute("data-illustration", image.asset)
        art.setAttribute("aria-label", image.description)
      }
      Vector(conversation.first, conversation.second).zip(speakers).foreach {
        case (line, (name, speech)) =>
          if name.textContent != line.name then name.textContent = line.name
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
