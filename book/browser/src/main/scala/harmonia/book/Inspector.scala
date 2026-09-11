package harmonia.book

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import harmonia.book.ui.Elements.*
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

/** One owned, cancellable inspector; closing returns focus to the selected story link. */
final class Inspector(dispatcher: Dispatcher[IO]):
  private val dialog = element("dialog", "inspector")
  private val heading = element("h2"); heading.id = "inspector-title"
  private val content = element("pre"); content.tabIndex = 0
  private val original = element("a").asInstanceOf[dom.html.Anchor]
  original.textContent = "Open original file"; original.target = "_blank"; original.rel = "noopener"
  private var cancel: () => Unit = () => ()
  private var dismiss: () => Unit = () => ()
  private var selected: Option[String] = None
  private val close = button("Return to the story", "button primary", "inspector-close") {
    dialog.asInstanceOf[js.Dynamic].close()
  }
  dialog.setAttribute("aria-labelledby", heading.id)
  dialog.addEventListener(
    "close",
    (_: dom.Event) => if !dialog.hasAttribute("open") then { cancel(); selected = None; dismiss() }
  )
  append(dialog, close, heading, original, content)
  append(dom.document.body, dialog)

  def hide(): Unit =
    dismiss = () => ()
    selected = None
    cancel()
    if dialog.hasAttribute("open") then dialog.asInstanceOf[js.Dynamic].close()

  def show(path: String, title: String, onDismiss: () => Unit): Unit =
    dismiss = onDismiss
    if !selected.contains(path) then
      selected = Some(path)
      open(path, title)

  private def open(path: String, title: String): Unit =
    cancel()
    heading.textContent = title; original.href = path;
    content.textContent = "Reading the recorded file…"
    if !dialog.hasAttribute("open") then dialog.asInstanceOf[js.Dynamic].showModal()
    close.focus()
    val bundled = js.Dynamic.global.selectDynamic("HarmoniaEvidenceFiles")
    val local = if js.isUndefined(bundled) then js.undefined else bundled.selectDynamic(path)
    val request = if !js.isUndefined(local) then IO { content.textContent = local.toString }
    else
      Resource
        .make(IO(new dom.AbortController()))(c => IO(c.abort()))
        .use { controller =>
          for
            response <- IO.fromFuture(
              IO(dom.fetch(path, new dom.RequestInit { signal = controller.signal }).toFuture)
            )
            _ <- IO.raiseUnless(response.ok)(
              RuntimeException(s"File unavailable (HTTP ${response.status})")
            )
            text <- IO.fromFuture(IO(response.text().toFuture))
            _ <- IO { content.textContent = text }
          yield ()
        }
        .timeout(15.seconds)
        .handleErrorWith(e => IO { content.textContent = e.getMessage })
    val stop = dispatcher.unsafeRunCancelable(request)
    cancel = () => { stop(); () }

  def dispose(): Unit =
    cancel(); dialog.remove()
