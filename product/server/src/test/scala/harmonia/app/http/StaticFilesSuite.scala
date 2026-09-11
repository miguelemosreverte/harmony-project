package harmonia.app.http

import munit.FunSuite
import java.nio.file.Files

class StaticFilesSuite extends FunSuite:
  test("a static directory serves only regular files inside its real directory") {
    val root = Files.createTempDirectory("harmonia-static")
    val outside = Files.createTempFile("harmonia-private", ".txt")
    try
      Files.writeString(root.resolve("index.html"), "book")
      Files.createSymbolicLink(root.resolve("escape.txt"), outside)
      assertEquals(StaticFiles.resolve(root, "").map(_._2), Some("text/html"))
      assertEquals(StaticFiles.resolve(root, "../" + outside.getFileName), None)
      assertEquals(StaticFiles.resolve(root, "escape.txt"), None)
      assertEquals(StaticFiles.resolve(root, outside.toString), None)
      assertEquals(StaticFiles.resolve(root, "missing.html"), None)
    finally
      Files.deleteIfExists(root.resolve("escape.txt"))
      Files.deleteIfExists(root.resolve("index.html"))
      Files.deleteIfExists(root)
      Files.deleteIfExists(outside)
  }
