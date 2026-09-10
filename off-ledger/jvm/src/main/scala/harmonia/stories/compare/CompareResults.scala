package harmonia.stories.compare

import io.circe.Json

final case class Difference(path: String, expected: Option[Json], actual: Option[Json])

object CompareResults:
  def compare(expected: Json, actual: Json, path: String = "$"): Vector[Difference] =
    if expected == actual then Vector.empty
    else
      (expected.asObject, actual.asObject, expected.asArray, actual.asArray) match
        case (Some(left), Some(right), _, _) =>
          (left.keys.toSet ++ right.keys.toSet).toVector.sorted.flatMap { key =>
            (left(key), right(key)) match
              case (Some(a), Some(b)) => compare(a, b, s"$path.$key")
              case (a, b)             => Vector(Difference(s"$path.$key", a, b))
          }
        case (_, _, Some(left), Some(right)) =>
          (0 until math.max(left.size, right.size)).toVector.flatMap { index =>
            (left.lift(index), right.lift(index)) match
              case (Some(a), Some(b)) => compare(a, b, s"$path[$index]")
              case (a, b)             => Vector(Difference(s"$path[$index]", a, b))
          }
        case _ => Vector(Difference(path, Some(expected), Some(actual)))

  def markdown(differences: Vector[Difference]): String =
    def cell(value: Option[Json]): String =
      value.map(_.noSpaces).getOrElse("(missing)").replace("|", "\\|").replace("\n", " ")
    if differences.isEmpty then
      "# Comparison\n\nObserved result matches the committed expectation.\n"
    else
      "# Comparison\n\n| Field | Expected | Actual |\n| --- | --- | --- |\n" +
        differences.map(d => s"| ${d.path} | ${cell(d.expected)} | ${cell(d.actual)} |\n").mkString
