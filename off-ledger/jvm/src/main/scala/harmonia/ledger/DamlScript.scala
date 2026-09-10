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
    execute(
      root,
      dar,
      script,
      artifacts,
      input,
      List("--ledger-host", "localhost", "--ledger-port", ledger.port.toString)
    )

  def runNetwork(
      root: Path,
      configuration: Path,
      dar: Path,
      script: String,
      artifacts: Path,
      input: Path
  ): IO[Path] =
    execute(
      root,
      dar,
      script,
      artifacts,
      Some(input),
      List("--participant-config", configuration.toString)
    )

  private def execute(
      root: Path,
      dar: Path,
      script: String,
      artifacts: Path,
      input: Option[Path],
      connection: List[String]
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
          "--output-file",
          output.toString
        ) ++ connection ++ input.toList.flatMap(path => List("--input-file", path.toString)),
        root,
        artifacts.resolve("script.log")
      )
      .as(output)
