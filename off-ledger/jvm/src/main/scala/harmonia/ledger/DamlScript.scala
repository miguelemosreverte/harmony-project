package harmonia.ledger

import cats.effect.IO
import harmonia.processes.ManagedProcess
import java.nio.file.Path

object DamlScript:
  def run(
      root: Path,
      ledger: CantonSandbox,
      dar: Path,
      script: String,
      artifacts: Path,
      input: Option[Path] = None
  ): IO[Path] =
    val output = artifacts.resolve("observation.json")
    ManagedProcess
      .run(
        List(
          root.resolve("scripts/daml").toString,
          "script",
          "--dar",
          dar.toString,
          "--script-name",
          script,
          "--ledger-host",
          "localhost",
          "--ledger-port",
          ledger.port.toString,
          "--output-file",
          output.toString
        ) ++ input.toList.flatMap(path => List("--input-file", path.toString)),
        root,
        artifacts.resolve("script.log")
      )
      .as(output)
