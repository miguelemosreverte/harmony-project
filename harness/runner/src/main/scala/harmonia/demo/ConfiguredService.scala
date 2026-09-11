package harmonia.demo

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import harmonia.app.http.LiveServer
import harmonia.files.ArtifactFiles
import harmonia.processes.ManagedProcess
import java.net.URI
import java.nio.file.Path
import scala.concurrent.duration.*

/** Exercise the standalone product against an already provisioned test network. */
object ConfiguredService:
  def resource(root: Path, configuration: Path, output: Path): Resource[IO, LiveServer] =
    Resource.eval(ArtifactFiles.read(root.resolve(".artifacts/classpaths/service.txt"))).flatMap {
      classpath =>
        ManagedProcess
          .start(
            List(
              Path.of(sys.props("java.home"), "bin", "java").toString,
              "-Xms32m",
              "-Xmx512m",
              "-XX:ActiveProcessorCount=4",
              "-cp",
              classpath.trim,
              "harmonia.app.Main",
              "serve",
              configuration.toString,
              output.toString
            ),
            root,
            output.resolve("service.log")
          )
          .evalMap { process =>
            def ready: IO[LiveServer] = (for
              text <- ArtifactFiles.read(output.resolve("sessions.json"))
              links <- IO.fromEither(io.circe.parser.decode[Map[String, String]](text))
              _ <- IO.raiseUnless(links.keySet == Set("bank", "buyer", "reviewer"))(
                RuntimeException("Participant sessions are not ready")
              )
              addresses = links.map((name, link) => name -> URI.create(link))
            yield LiveServer(
              addresses.values.head.getPort,
              addresses.map((name, uri) => name -> uri.getFragment.stripPrefix("session="))
            ))
              .handleErrorWith { _ =>
                IO.raiseUnless(process.isAlive)(
                  RuntimeException(s"Service exited; see $output/service.log")
                ) *>
                  IO.sleep(100.millis) *> ready
              }
            ready.timeout(40.seconds)
          }
    }
