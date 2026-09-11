ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "io.harmonia"
ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

lazy val exportRuntime = taskKey[File]("Export immutable runtime JARs and their classpath")

lazy val json = Seq(
  "io.circe" %% "circe-core" % "0.14.10",
  "io.circe" %% "circe-parser" % "0.14.10"
)
lazy val jvmSettings = Seq(
  exportJars := true,
  libraryDependencies ++= json :+ ("org.typelevel" %% "cats-effect" % "3.6.3"),
  libraryDependencies += "org.scalameta" %% "munit" % "1.0.4" % Test,
  exportRuntime := {
    val jars = (Compile / packageBin).value +: (Compile / dependencyClasspath).value.files
    require(jars.forall(_.isFile), "Runtime dependencies must be packaged JARs")
    val stored = jars.map { jar =>
      val hash = java.security.MessageDigest.getInstance("SHA-256").digest(IO.readBytes(jar)).map(b => f"${b & 0xff}%02x").mkString
      val target = file(".artifacts/runtime") / hash / jar.getName
      if (!target.exists) IO.copyFile(jar, target)
      target.getAbsolutePath
    }
    val manifest = file(".artifacts/classpaths") / (thisProject.value.id + ".txt")
    IO.write(manifest, stored.mkString(java.io.File.pathSeparator))
    manifest
  }
)
lazy val browserSettings = Seq(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-effect" % "3.6.3",
    "io.circe" %%% "circe-core" % "0.14.10",
    "io.circe" %%% "circe-parser" % "0.14.10",
    "org.scala-js" %%% "scalajs-dom" % "2.8.1"
  )
)

// The product has no dependency on book or harness projects.
lazy val service = project.in(file("product/server")).settings(jvmSettings).settings(
  name := "harmonia-service",
  Compile / mainClass := Some("harmonia.app.Main"),
  Compile / unmanagedSourceDirectories += file("product/api/src/main/scala").getAbsoluteFile,
  libraryDependencies ++= Seq(
    "com.daml" % "ledger-api-java-proto" % "3.4.11",
    "com.daml" % "daml-lf-archive-java-proto" % "3.4.11",
    "io.grpc" % "grpc-netty-shaded" % "1.77.0",
    "io.grpc" % "grpc-protobuf" % "1.77.0",
    "io.grpc" % "grpc-stub" % "1.77.0",
    "com.google.protobuf" % "protobuf-java-util" % "3.25.5",
    "org.snakeyaml" % "snakeyaml-engine" % "2.9",
    "org.commonmark" % "commonmark" % "0.24.0"
  )
)
lazy val scene = project.in(file("product/scene")).enablePlugins(ScalaJSPlugin).settings(browserSettings).settings(
  name := "harmonia-scene",
  libraryDependencies += "org.scalameta" %%% "munit" % "1.0.4" % Test,
  scalaJSUseMainModuleInitializer := false
)
lazy val web = project.in(file("product/web")).dependsOn(scene).enablePlugins(ScalaJSPlugin).settings(browserSettings).settings(
  name := "harmonia-web",
  Compile / mainClass := Some("harmonia.live.Main"),
  Compile / unmanagedSourceDirectories += file("product/api/src/main/scala").getAbsoluteFile
)

lazy val runner = project.in(file("harness/runner")).dependsOn(service).settings(jvmSettings).settings(
  name := "harmonia-runner",
  Compile / unmanagedSourceDirectories += file("harness/model/src/main/scala").getAbsoluteFile
)
lazy val bookExport = project.in(file("book/export")).dependsOn(runner).settings(jvmSettings).settings(
  name := "harmonia-book-export",
  Compile / mainClass := Some("harmonia.book.Main"),
  Compile / unmanagedSourceDirectories += file("book/model/src/main/scala").getAbsoluteFile,
  libraryDependencies += "org.commonmark" % "commonmark" % "0.24.0"
)
lazy val reader = project.in(file("book/browser")).dependsOn(scene).enablePlugins(ScalaJSPlugin).settings(browserSettings).settings(
  name := "harmonia-reader",
  libraryDependencies += "org.scalameta" %%% "munit" % "1.0.4" % Test,
  Compile / mainClass := Some("harmonia.book.BookApp"),
  Compile / unmanagedSourceDirectories ++= Seq(file("book/model/src/main/scala").getAbsoluteFile, file("harness/model/src/main/scala").getAbsoluteFile)
)
lazy val tools = project.in(file("harness/tools")).dependsOn(bookExport).settings(jvmSettings).settings(
  name := "harmonia-tools",
  exportRuntime := exportRuntime.dependsOn(service / exportRuntime, bookExport / exportRuntime).value,
  Compile / mainClass := Some("harmonia.tools.Main")
)
lazy val root = project.in(file(".")).aggregate(service, scene, web, runner, bookExport, reader, tools).settings(
  name := "harmonia",
  publish / skip := true
)
