package harmonia.packages.read

import cats.syntax.all.*
import harmonia.files.MarkdownYaml
import java.net.URI

final case class PackageInput(
    name: String,
    source: String,
    sha256: String,
    packageId: String,
    lf: String
)

object PackageManifest:
  def read(markdown: String): Either[String, Vector[PackageInput]] = for
    json <- MarkdownYaml.read(markdown, "Packages")
    entries <- json.asObject.toRight("Packages must map names to pinned inputs")
    _ <- Either.cond(entries.nonEmpty && entries.size <= 16, (), "Declare 1 to 16 packages")
    result <- entries.toVector.traverse { (name, value) =>
      for
        _ <- Either.cond(name.matches("[a-z][a-z0-9-]{0,63}"), (), s"Invalid package alias: $name")
        obj <- value.asObject.toRight(s"$name must be a mapping")
        _ <- Either.cond(
          obj.keys.toSet == Set("source", "sha256", "package_id", "lf"),
          (),
          s"$name requires exactly source, sha256, package_id, and lf"
        )
        source <- value.hcursor.get[String]("source").left.map(_.message)
        sha256 <- value.hcursor.get[String]("sha256").left.map(_.message)
        packageId <- value.hcursor.get[String]("package_id").left.map(_.message)
        lf <- value.hcursor.get[String]("lf").left.map(_.message)
        _ <- Either.cond(
          sha256.matches("[a-f0-9]{64}") && packageId.matches("[a-f0-9]{64}"),
          (),
          s"$name requires complete lowercase SHA-256 and package identifiers"
        )
        _ <- Either.cond(
          Set("2.1", "2.2")(lf),
          (),
          s"$name: supported Daml-LF versions are 2.1 and 2.2, found $lf"
        )
        _ <- validateSource(source)
      yield PackageInput(name, source, sha256, packageId, lf)
    }
  yield result

  def validateSource(source: String): Either[String, Unit] =
    if source.contains(":") then
      Either.catchNonFatal(URI.create(source)).left.map(_.getMessage).flatMap { uri =>
        Either.cond(
          uri.getScheme == "https" && uri.getHost == "raw.githubusercontent.com" &&
            uri.getPort == -1 && uri.getUserInfo == null && uri.getQuery == null && uri.getFragment == null &&
            uri.getPath
              .matches("/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/[a-f0-9]{40}/[A-Za-z0-9_./-]+\\.dar") &&
            !uri.getPath.split('/').contains(".."),
          (),
          "External DAR source must be a raw.githubusercontent.com HTTPS URL pinned to a full commit ID"
        )
      }
    else
      Either.cond(
        source.nonEmpty && source.endsWith(".dar"),
        (),
        "Local package source must name a DAR file"
      )
