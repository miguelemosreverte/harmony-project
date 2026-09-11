package harmonia.packages.workspace

import cats.effect.unsafe.implicits.global
import harmonia.bindings.generate.GeneratedProject
import io.circe.Json
import java.nio.file.Files
import java.util.zip.ZipFile
import munit.FunSuite
import scala.jdk.CollectionConverters.*

class ProjectArchiveSuite extends FunSuite:
  test("archive membership comes from generated facts and rejects an escaping member") {
    val directory = Files.createTempDirectory("harmonia-archive-test-")
    val root = directory.resolve("project")
    val names = Vector(
      "binding.daml",
      "mapping.md",
      "generation.json",
      "vendor/source.dar",
      "vendor/interfaces.dar",
      "vendor/core.dar",
      "library/output.dar",
      "example/output.dar"
    )
    try
      names.foreach { name =>
        val file = root.resolve(name)
        Files.createDirectories(file.getParent); Files.writeString(file, name)
      }
      val project = GeneratedProject(
        root,
        root.resolve("library/output.dar"),
        root.resolve("example/output.dar"),
        "digest",
        Vector("binding.daml"),
        Json.Null
      )
      val path = ProjectArchive.write(project).unsafeRunSync()
      val zip = new ZipFile(path.toFile)
      try assertEquals(zip.entries().asScala.map(_.getName).toSet, names.toSet)
      finally zip.close()
      Files.writeString(directory.resolve("outside.daml"), "outside")
      intercept[IllegalArgumentException] {
        ProjectArchive.write(project.copy(members = Vector("../outside.daml"))).unsafeRunSync()
      }
    finally
      val files = Files.walk(directory)
      try files.iterator().asScala.toVector.sortBy(_.getNameCount).reverse.foreach(Files.delete)
      finally files.close()
  }
