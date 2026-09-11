ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "io.harmonia"
ThisBuild / version := "0.1.0"

lazy val exportRuntime = taskKey[File]("Export an immutable application JAR and runtime classpath for direct JVM launch")

lazy val jvm = project.in(file("jvm")).settings(
  name := "harmonia-tools",
  Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.6.3",
    "io.circe" %% "circe-core" % "0.14.10",
    "io.circe" %% "circe-parser" % "0.14.10",
    "org.commonmark" % "commonmark" % "0.24.0",
    "com.daml" % "ledger-api-java-proto" % "3.4.11",
    "io.grpc" % "grpc-netty-shaded" % "1.77.0",
    "io.grpc" % "grpc-protobuf" % "1.77.0",
    "io.grpc" % "grpc-stub" % "1.77.0",
    "com.google.protobuf" % "protobuf-java-util" % "3.25.5",
    "org.snakeyaml" % "snakeyaml-engine" % "2.9",
    "org.scalameta" %% "munit" % "1.0.4" % Test
  ),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  Compile / run / fork := true,
  Compile / run / javaOptions ++= Seq("-Xms32m", "-Xmx512m", "-XX:ActiveProcessorCount=4"),
  exportRuntime := {
    val jar = (Compile / packageBin).value
    val digest = java.security.MessageDigest.getInstance("SHA-256").digest(IO.readBytes(jar)).map(b => f"${b & 0xff}%02x").mkString
    val workspace = baseDirectory.value.getParentFile.getParentFile
    val executable = workspace / ".artifacts" / "runtime" / digest / "harmonia.jar"
    if (!executable.exists) IO.copyFile(jar, executable)
    val dependencies = (Compile / dependencyClasspath).value.files
    val manifest = baseDirectory.value.getParentFile / "target" / "runtime-classpath"
    IO.write(manifest, (executable +: dependencies).map(_.getAbsolutePath).mkString(java.io.File.pathSeparator))
    manifest
  },
  Compile / run / connectInput := false
)

lazy val browser = project.in(file("browser")).enablePlugins(ScalaJSPlugin).settings(
  name := "harmonia-book",
  scalaJSUseMainModuleInitializer := true,
  Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala",
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-effect" % "3.6.3",
    "io.circe" %%% "circe-core" % "0.14.10",
    "io.circe" %%% "circe-parser" % "0.14.10",
    "org.scala-js" %%% "scalajs-dom" % "2.8.1"
  )
)

lazy val root = project.in(file(".")).aggregate(jvm, browser).settings(
  name := "harmonia-off-ledger",
  publish / skip := true
)
