package harmonia.scene

import io.circe.Codec
import org.scalajs.dom

/** An observed attempt can leave state unchanged; its identity still belongs on the diagram. */
final case class SceneObservation(actor: String, action: String, outcome: String)
object SceneObservation:
  given Codec.AsObject[SceneObservation] = Codec.AsObject.derived[SceneObservation]

final class ObservationView(parent: dom.HTMLElement):
  private val strip = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
  strip.className = "scene-observation"
  Vector("observation-actor", "observation-action", "observation-outcome").foreach { css =>
    val span = dom.document.createElement("span"); span.setAttribute("class", css);
    strip.appendChild(span)
  }
  strip.setAttribute("aria-live", "polite")
  parent.appendChild(strip)

  def render(value: Option[SceneObservation]): Unit =
    if value.isEmpty then strip.setAttribute("hidden", "") else strip.removeAttribute("hidden")
    value.foreach { observation =>
      strip.setAttribute("data-outcome", observation.outcome)
      strip.children(0).textContent = observation.actor
      strip.children(1).textContent = observation.action
      strip.children(2).textContent = observation.outcome
    }
