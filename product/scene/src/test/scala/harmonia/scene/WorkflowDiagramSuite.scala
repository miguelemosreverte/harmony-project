package harmonia.scene

import munit.FunSuite

class WorkflowDiagramSuite extends FunSuite:
  private def diagram(ids: Vector[String], edges: Vector[(String, String)]): WorkflowDiagram =
    WorkflowDiagram(
      "",
      "",
      ids.map(id => DiagramNode(id, id, "", "", DiagramState.Pending)),
      edges.map((from, to) => DiagramEdge(from, to, DiagramState.Pending))
    )

  test("independent preparation branches converge before settlement") {
    val value = diagram(
      Vector("agreement", "source", "destination", "settle"),
      Vector(
        "agreement" -> "source",
        "agreement" -> "destination",
        "source" -> "settle",
        "destination" -> "settle"
      )
    )
    assertEquals(
      value.layers,
      Right(Map("agreement" -> 0, "source" -> 1, "destination" -> 1, "settle" -> 2))
    )
  }
  test("unknown endpoints and cycles cannot masquerade as a valid plan") {
    assert(diagram(Vector("a"), Vector("a" -> "missing")).layers.isLeft)
    assert(diagram(Vector("a", "b"), Vector("a" -> "b", "b" -> "a")).layers.isLeft)
    assert(diagram(Vector("a", "a"), Vector.empty).layers.isLeft)
  }
