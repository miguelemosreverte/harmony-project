package harmonia.app.http

import java.nio.file.{Files, Path}

/** A mounted document tree cannot expose files outside its real directory. */
object StaticFiles:
  def resolve(directory: Path, requested: String): Option[(Path, String)] =
    val root = directory.toAbsolutePath.normalize()
    val file = root.resolve(if requested.isEmpty then "index.html" else requested).normalize()
    Option.when(
      file.startsWith(root) && Files
        .isRegularFile(file) && file.toRealPath().startsWith(root.toRealPath())
    )(file -> contentType(file))

  def contentType(file: Path): String = file.getFileName.toString.split('.').last match
    case "html" => "text/html"
    case "css"  => "text/css"
    case "js"   => "text/javascript"
    case "json" => "application/json"
    case "svg"  => "image/svg+xml"
    case "png"  => "image/png"
    case "pdf"  => "application/pdf"
    case _      => "text/plain"
