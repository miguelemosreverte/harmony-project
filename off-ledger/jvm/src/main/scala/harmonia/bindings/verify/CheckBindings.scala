package harmonia.bindings.verify

import cats.effect.IO
import cats.syntax.all.*
import java.nio.file.Path

object CheckBindings:
  def run(root: Path): IO[Unit] = Vector(
    "financing" -> "generated-financing",
    "primitive-approval" -> "generated-primitive-approval"
  ).traverse_ { (mapping, output) =>
    CheckBinding.run(
      root,
      root.resolve(s"packages/mappings/$mapping.md"),
      root.resolve("packages/mappings/approval-expected.md"),
      root.resolve(s".artifacts/$output")
    )
  }
