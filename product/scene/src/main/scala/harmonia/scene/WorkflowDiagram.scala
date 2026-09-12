package harmonia.scene

import io.circe.{Codec, Decoder, Encoder}

/** A view of a plan or observation. This model has no command or ledger authority. */
enum DiagramState:
  case Pending, Current, Complete, Refused, Skipped
object DiagramState:
  given Encoder[DiagramState] = Encoder.encodeString.contramap(_.toString.toLowerCase)
  given Decoder[DiagramState] = Decoder.decodeString.emap(value =>
    DiagramState.values.find(_.toString.equalsIgnoreCase(value)).toRight("Unknown diagram state")
  )

final case class DiagramNode(
    id: String,
    label: String,
    detail: String,
    actor: String,
    state: DiagramState
)
object DiagramNode:
  given Codec.AsObject[DiagramNode] = Codec.AsObject.derived[DiagramNode]

final case class DiagramEdge(from: String, to: String, state: DiagramState)
object DiagramEdge:
  given Codec.AsObject[DiagramEdge] = Codec.AsObject.derived[DiagramEdge]

final case class WorkflowDiagram(
    title: String,
    caption: String,
    nodes: Vector[DiagramNode],
    edges: Vector[DiagramEdge],
    observation: Option[SceneObservation] = None
):
  /** Explicit edges determine layers. No text matching is used to infer dependencies. */
  def layers: Either[String, Map[String, Int]] =
    val ids = nodes.map(_.id).toSet
    if ids.size != nodes.size || nodes.isEmpty then Left("A diagram needs distinct nodes")
    else if edges.exists(e => !ids(e.from) || !ids(e.to)) then
      Left("A connection refers to a missing node")
    else
      var remaining = ids
      var placed = Map.empty[String, Int]
      while remaining.nonEmpty do
        val ready =
          remaining.filter(id => edges.filter(_.to == id).forall(e => placed.contains(e.from)))
        if ready.isEmpty then return Left("A diagram contains a cycle")
        ready.foreach { id =>
          placed = placed.updated(
            id,
            edges.filter(_.to == id).map(e => placed(e.from) + 1).maxOption.getOrElse(0)
          )
        }
        remaining --= ready
      Right(placed)
object WorkflowDiagram:
  given Codec.AsObject[WorkflowDiagram] = Codec.AsObject.derived[WorkflowDiagram]
