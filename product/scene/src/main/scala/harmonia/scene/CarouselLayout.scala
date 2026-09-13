package harmonia.scene

import io.circe.Codec

/** Shared prefixes are declared by the owner of the story, never inferred from labels. */
final case class CarouselStep(id: String, label: String, state: DiagramState)
object CarouselStep:
  given Codec.AsObject[CarouselStep] = Codec.AsObject.derived[CarouselStep]

final case class CarouselPath(
    id: String,
    label: String,
    steps: Vector[CarouselStep],
    sharedPrefix: Option[Int] = None,
    outcome: Option[DiagramState] = None,
    answers: Option[Boolean] = None
)
object CarouselPath:
  given Codec.AsObject[CarouselPath] = Codec.AsObject.derived[CarouselPath]

final case class CarouselFrame(paths: Vector[CarouselPath], selected: String)
object CarouselFrame:
  given Codec.AsObject[CarouselFrame] = Codec.AsObject.derived[CarouselFrame]

final case class CarouselPoint(
    step: CarouselStep,
    column: Int,
    row: Int,
    terminal: Option[DiagramState]
)
final case class CarouselLink(from: String, to: String, state: DiagramState)
final case class CarouselLayout(
    points: Vector[CarouselPoint],
    links: Vector[CarouselLink],
    selected: String,
    addresses: Map[String, String]
)

object CarouselLayout:
  def apply(frame: CarouselFrame): CarouselLayout =
    val paths = frame.paths.filterNot(_.answers.contains(true))
    require(paths.nonEmpty && paths.forall(_.steps.nonEmpty), "A route needs at least one stop")
    val primary = paths.head
    val active = paths.find(_.steps.exists(_.id == frame.selected)).getOrElse(primary)
    def canonical(path: CarouselPath, index: Int): String =
      if index < path.sharedPrefix.getOrElse(0) then primary.steps(index).id
      else path.steps(index).id
    val points = paths.zipWithIndex.flatMap { (path, row) =>
      val shared = path.sharedPrefix.getOrElse(0)
      require(
        shared >= 0 && shared < path.steps.size && shared <= primary.steps.size,
        "Invalid shared prefix"
      )
      path.steps.zipWithIndex.drop(shared).map { (step, column) =>
        val observed =
          if row == 0 && column < active.sharedPrefix.getOrElse(0) then active.steps(column)
          else step
        CarouselPoint(
          step.copy(state = observed.state),
          column,
          row,
          if column == path.steps.size - 1 then path.outcome else None
        )
      }
    }
    val links = paths
      .flatMap { path =>
        path.steps.indices.drop(1).map { i =>
          val state =
            if path == active && i <= path.steps.indexWhere(_.id == frame.selected) then
              path.steps(i).state
            else DiagramState.Pending
          CarouselLink(canonical(path, i - 1), canonical(path, i), state)
        }
      }
      .groupBy(link => link.from -> link.to)
      .values
      .map(links => links.find(_.state != DiagramState.Pending).getOrElse(links.head))
      .toVector
      .sortBy(link => link.from -> link.to)
    val index = active.steps.indexWhere(_.id == frame.selected).max(0)
    val addresses = points.map { point =>
      val equivalent = active.steps.indices.find(i => canonical(active, i) == point.step.id)
      point.step.id -> equivalent.map(active.steps(_).id).getOrElse(point.step.id)
    }.toMap
    CarouselLayout(points, links, canonical(active, index), addresses)
