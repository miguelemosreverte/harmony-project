package harmonia.packages.workspace

import cats.effect.IO
import harmonia.bindings.generate.GeneratedProject
import io.circe.Json
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

object ProjectArchive:
  def write(project: GeneratedProject, manifest: Json): IO[Path] = IO.blocking {
    val root = project.directory.toAbsolutePath.normalize()
    val generated = manifest.hcursor.downField("files").keys.getOrElse(Vector.empty).toVector
    val files = (generated ++ Vector(
      "mapping.md",
      "generation.json",
      "vendor/source.dar",
      "vendor/interfaces.dar",
      "vendor/core.dar",
      root.relativize(project.library.toAbsolutePath).toString,
      root.relativize(project.example.toAbsolutePath).toString
    )).distinct.sorted
    require(files.size <= 64, "Generated archive has too many files")
    val output = root.getParent.resolve("project.zip")
    val zip = new ZipOutputStream(Files.newOutputStream(output))
    try
      files.foreach { name =>
        val source = root.resolve(name).normalize()
        require(
          source.startsWith(root) && Files.isRegularFile(source),
          "Invalid generated project member"
        )
        val entry = new ZipEntry(name); entry.setTime(0)
        zip.putNextEntry(entry); Files.copy(source, zip); zip.closeEntry()
      }
    finally zip.close()
    output
  }
