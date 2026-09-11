package harmonia.book

import scala.scalajs.js.URIUtils.{encodeURIComponent, decodeURIComponent}
import scala.util.Try

object BookNavigation:
  def address(
      state: ViewState,
      stories: Vector[RecordedStory],
      chapters: Vector[BookChapter]
  ): String =
    val fields = state.chapter match
      case Some(index) => Vector("chapter" -> chapters(index).id)
      case None =>
        Vector("story" -> stories(state.story).id, "step" -> state.step.toString) ++
          state.perspective.map("perspective" -> _) ++ state.originChapter.map(i =>
            "from" -> chapters(i).id
          )
    "#" + fields.map((key, value) => key + "=" + encodeURIComponent(value)).mkString("&")

  def read(
      fragment: String,
      stories: Vector[RecordedStory],
      chapters: Vector[BookChapter]
  ): ViewState =
    val initial =
      ViewState(math.max(0, stories.indexWhere(_.id == "financing-approved")), 0, Some(0))
    Try {
      val fields = fragment.stripPrefix("#").split('&').toVector.filter(_.nonEmpty).map { part =>
        val pair = part.split("=", 2)
        require(pair.length == 2, "Malformed reader address")
        decodeURIComponent(pair(0)) -> decodeURIComponent(pair(1))
      }
      require(fields.map(_._1).distinct.size == fields.size, "Repeated reader address field")
      val values = fields.toMap
      values.get("story") match
        case Some(id) =>
          val story = stories.indexWhere(_.id == id); require(story >= 0, "Unknown story")
          val step = values.get("step").flatMap(_.toIntOption).getOrElse(0)
          val origin = values.get("from").map(id => chapters.indexWhere(_.id == id)).filter(_ >= 0)
          ViewState(
            story,
            math.max(0, math.min(step, stories(story).units.size - 1)),
            None,
            values.get("perspective"),
            origin
          )
        case None =>
          val chapter = values
            .get("chapter")
            .map(id => chapters.indexWhere(_.id == id))
            .filter(_ >= 0)
            .getOrElse(0)
          initial.copy(chapter = Some(chapter))
    }.getOrElse(initial)
