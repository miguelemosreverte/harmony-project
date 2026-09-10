ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "io.harmonia"
ThisBuild / version := "0.1.0"

lazy val jvm = project.in(file("jvm")).settings(
  name := "harmonia-tools",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.6.3",
    "io.circe" %% "circe-core" % "0.14.10",
    "io.circe" %% "circe-parser" % "0.14.10",
    "org.commonmark" % "commonmark" % "0.24.0",
    "org.snakeyaml" % "snakeyaml-engine" % "2.9",
    "org.scalameta" %% "munit" % "1.0.4" % Test
  ),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  Compile / run / fork := true,
  Compile / run / connectInput := false
)

lazy val root = project.in(file(".")).aggregate(jvm).settings(
  name := "harmonia-off-ledger",
  publish / skip := true
)
