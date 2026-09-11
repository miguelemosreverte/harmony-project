package harmonia.book

import munit.FunSuite
import harmonia.book.diagram.{RecordedScene, StoryDiagram, ChapterDiagram}
import harmonia.examples.{Examples, ExampleKind}
import harmonia.scene.DiagramState
import io.circe.Json
import scala.scalajs.js

class DiagramSuite extends FunSuite:
  private val fs = js.Dynamic.global.require("fs")
  private val root =
    js.Dynamic.global.process.env.HARMONIA_ROOT.asInstanceOf[js.UndefOr[String]].getOrElse(".")
  private def read(path: String) = fs.readFileSync(root + "/" + path, "utf8").toString
  private def recording(id: String): RecordedStory = io.circe.parser
    .decode[RecordedStory](read(s"book/edition-0.2/recordings/$id.json"))
    .fold(throw _, identity)

  test("all registered recordings and all 130 observed moments produce valid shared diagrams") {
    val stories = Examples.all.map(example => recording(example.id))
    assertEquals(stories.map(_.kind).toSet, ExampleKind.values.toSet)
    assertEquals(stories.map(_.units.size).sum, 130)
    stories.foreach { story =>
      story.units.indices.foreach { index =>
        assert(StoryDiagram(story, index).layers.isRight, story.id + ":" + index)
        if Set(ExampleKind.Purchase, ExampleKind.Transfer)(story.kind) then
          val scene = RecordedScene(story, index + 1)
          assertEquals(scene.people.size, 4)
          assert(scene.phase >= 0 && scene.phase <= 4)
      }
    }
  }

  test("changing every expectation cannot change the observed diagram or scene") {
    Examples.all.foreach { example =>
      val story = recording(example.id)
      val altered = story.copy(
        expected = Json.Null,
        presentation =
          story.presentation.copy(units = story.units.map(_.copy(expected = Json.Null)))
      )
      story.units.indices.foreach { index =>
        assertEquals(StoryDiagram(altered, index), StoryDiagram(story, index), story.id)
        if Set(ExampleKind.Purchase, ExampleKind.Transfer)(story.kind) then
          assertEquals(RecordedScene(altered, index + 1), RecordedScene(story, index + 1), story.id)
      }
    }
  }

  test("an approved branch joins financing and review while closure stays skipped") {
    val story = recording("branch-approved")
    val graph = StoryDiagram(story, story.units.size - 1)
    assertEquals(graph.nodes.find(_.id == "closure").get.state, DiagramState.Skipped)
    assertEquals(graph.nodes.find(_.id == "join").get.state, DiagramState.Complete)
    assertEquals(
      graph.edges.filter(_.to == "join").map(_.from).toSet,
      Set("financing", "review", "closure")
    )
    assertEquals(graph.edges.find(_.from == "closure").get.state, DiagramState.Skipped)
  }

  test("a refusal cannot erase earlier completion or paint the refused attempt as success") {
    val story = recording("already-approved")
    val graph = StoryDiagram(story, 0)
    assertEquals(graph.nodes.find(_.id == "application").get.state, DiagramState.Complete)
    assertEquals(graph.nodes.find(_.id == "actor").get.state, DiagramState.Refused)
    assertEquals(graph.edges.head.state, DiagramState.Refused)
  }

  test("missing observations are not replaced by a golden success") {
    val story = recording("workflow-approved")
    val missing = story.copy(presentation =
      story.presentation.copy(units = story.units.map(_.copy(actual = Json.Null)))
    )
    assert(StoryDiagram(missing, 1).nodes.forall(_.state != DiagramState.Complete))
    val transfer = recording("transfer-approved")
    val absent = transfer.copy(presentation =
      transfer.presentation.copy(units = transfer.units.map(_.copy(actual = Json.Null)))
    )
    assert(RecordedScene(absent, 1).amounts.forall(_.value.startsWith("Not observed")))
  }

  test("current chapters use supported diagrams and unsupported programs remain source") {
    val diagrams = Examples.chapters.flatMap { name =>
      "(?s)```mermaid\\n(.*?)```".r
        .findAllMatchIn(read("book/" + name))
        .map(m => m.group(1))
        .toVector
    }
    assertEquals(diagrams.size, 8)
    diagrams.foreach(source => assert(ChapterDiagram.parse(source).nonEmpty, source))
    assert(ChapterDiagram.parse("flowchart LR\na --> b\nb --> a").isEmpty)
    assert(ChapterDiagram.parse("sequenceDiagram\na --> b").isEmpty)
    assert(ChapterDiagram.parse("flowchart LR\na --> b\nunrecognized syntax").isEmpty)
  }

  test("query addresses round-trip an embedded presentation and preserve legacy hashes") {
    val stories = Examples.all.map(example => recording(example.id))
    val chapters = Examples.chapters.map(id => BookChapter(id, id, ""))
    val state = ViewState(
      3,
      0,
      node = Some("application"),
      evidence = true,
      theme = "dark",
      text = "large",
      embed = true,
      present = true
    )
    assertEquals(
      BookNavigation.read(BookNavigation.address(state, stories, chapters), stories, chapters),
      state
    )
    assertEquals(
      BookNavigation.read("#story=branch-approved&step=999", stories, chapters).step,
      recording("branch-approved").units.size - 1
    )
  }
