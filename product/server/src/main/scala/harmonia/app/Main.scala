package harmonia.app

import cats.effect.{ExitCode, IO, IOApp}
import harmonia.bindings.generate.GenerateBinding
import harmonia.packages.resolve.ResolvePackages
import java.nio.file.Path

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    val root = Path.of(sys.env.getOrElse("HARMONIA_ROOT", ".")).toAbsolutePath.normalize()
    val operation = args match
      case List("serve", configuration, directory) =>
        ServerConfig.serve(root, root.resolve(configuration), Some(root.resolve(directory)))
      case List("serve", configuration) => ServerConfig.serve(root, root.resolve(configuration))
      case List("generate-bindings", mapping, output) =>
        GenerateBinding.run(root, root.resolve(mapping), root.resolve(output))
      case List("resolve-packages") =>
        ResolvePackages.run(root, root.resolve("product/packages/inputs.md"))
      case List("resolve-packages", manifest) => ResolvePackages.run(root, root.resolve(manifest))
      case _ =>
        IO.raiseError(
          IllegalArgumentException(
            "Expected serve configuration, generate-bindings mapping output, or resolve-packages [manifest]"
          )
        )
    operation.as(ExitCode.Success)
