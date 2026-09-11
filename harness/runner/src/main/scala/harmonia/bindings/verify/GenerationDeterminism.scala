package harmonia.bindings.verify

import cats.effect.IO
import cats.syntax.all.*
import harmonia.bindings.generate.GenerateBinding
import harmonia.files.ArtifactFiles
import io.circe.Json
import java.nio.file.Path

object GenerationDeterminism:
  def verify(root: Path): IO[Unit] = for
    artifacts <- ArtifactFiles.createRun(root, "generation-repeat")
    repeated <- GenerateBinding.run(
      root,
      root.resolve("product/packages/mappings/financing.md"),
      artifacts.resolve("project")
    )
    first <- json(root.resolve(".artifacts/generated-financing/generation.json"))
    second <- json(repeated.directory.resolve("generation.json"))
    _ <- IO.raiseUnless(first == second)(
      RuntimeException(
        "Identical mapping and DAR inputs did not produce identical source and compiled artifact manifests"
      )
    )
    _ <- Vector(
      ("generated-financing", "GeneratedFinancing", "Financing"),
      ("generated-primitive-approval", "GeneratedPrimitiveApproval", "Primitive")
    ).traverse_ { (project, module, specimen) =>
      Vector(
        s"library/daml/$module.daml" -> s"$module.daml",
        "example/daml/Example.daml" -> s"${specimen}Example.daml"
      ).traverse_ { (generated, reviewed) =>
        for
          actual <- ArtifactFiles.read(root.resolve(s".artifacts/$project/$generated"))
          expected <- ArtifactFiles.read(root.resolve(s"book/generated/$reviewed"))
          _ <- IO.raiseUnless(actual == expected)(
            RuntimeException(
              s"Generated code differs from the reviewed book specimen $reviewed; review the generator and specimen explicitly"
            )
          )
        yield ()
      }
    }
    _ <- ArtifactFiles.write(
      artifacts.resolve("verification.md"),
      "# Deterministic generation\n\nIdentical mapping and pinned inputs produced identical generated source, configuration, and compiled DAR hashes in a fresh output directory. Both current projects match the reviewed book specimens.\n"
    )
    _ <- IO.println(
      s"PASS deterministic generated sources and DARs, and reviewed code specimens: $artifacts"
    )
  yield ()

  private def json(path: Path): IO[Json] =
    ArtifactFiles.read(path).flatMap(text => IO.fromEither(io.circe.parser.parse(text)))
