package harmonia.scene

import munit.FunSuite

class CarouselLayoutSuite extends FunSuite:
  private def path(id: String, shared: Int = 0) = CarouselPath(
    id,
    id,
    (0 to 3).map(i => CarouselStep(s"$id:$i", s"Step $i", DiagramState.Pending)).toVector,
    sharedPrefix = Some(shared),
    outcome = Some(if id == "yes" then DiagramState.Complete else DiagramState.Refused)
  )

  test("a branch shares its declared prefix and keeps its own address when rewound") {
    val paths = Vector(path("yes"), path("no", 2))
    val layout = CarouselLayout(CarouselFrame(paths, "no:3"))
    assertEquals(
      layout.points.map(_.step.id),
      Vector("yes:0", "yes:1", "yes:2", "yes:3", "no:2", "no:3")
    )
    assertEquals(
      layout.links.map(e => e.from -> e.to).toSet,
      Set(
        "yes:0" -> "yes:1",
        "yes:1" -> "yes:2",
        "yes:2" -> "yes:3",
        "yes:1" -> "no:2",
        "no:2" -> "no:3"
      )
    )
    assertEquals(layout.addresses("yes:1"), "no:1")
    assertEquals(CarouselLayout(CarouselFrame(paths, "no:1")).selected, "yes:1")
    assertEquals(layout.points.find(_.step.id == "no:2").map(p => (p.column, p.row)), Some((2, 1)))
    assertEquals(layout.points.last.terminal, Some(DiagramState.Refused))
    assertEquals(layout.links.find(_.to == "yes:2").get.state, DiagramState.Pending)
  }

  test("shared progress uses the selected branch and answer inputs add no graph nodes") {
    val accepted = path("yes")
    val refused =
      path("no", 2).copy(steps = path("no").steps.map(_.copy(state = DiagramState.Refused)))
    val answer = path("answer").copy(answers = Some(true))
    val layout = CarouselLayout(CarouselFrame(Vector(accepted, refused, answer), "no:1"))
    assertEquals(layout.points.size, 6)
    assertEquals(layout.points(1).step.state, DiagramState.Refused)
    assertEquals(layout.links.find(_.to == "yes:1").get.state, DiagramState.Refused)
  }

  test("invalid shared prefixes fail rather than drawing a misleading graph") {
    intercept[IllegalArgumentException](
      CarouselLayout(CarouselFrame(Vector(path("yes"), path("no", 5)), "yes:0"))
    )
    intercept[IllegalArgumentException](CarouselLayout(CarouselFrame(Vector.empty, "missing")))
  }

  test("inspecting a future live stop cannot mark waiting handoffs as completed") {
    val layout = CarouselLayout(CarouselFrame(Vector(path("live")), "live:3"))
    assertEquals(layout.selected, "live:3")
    assert(layout.links.forall(_.state == DiagramState.Pending))
  }
