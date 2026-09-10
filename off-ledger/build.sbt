ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "io.harmonia"
ThisBuild / version := "0.1.0"

lazy val jvm = project.in(file("jvm")).settings(
  name := "harmonia-tools",
  libraryDependencies += "org.typelevel" %% "cats-effect" % "3.6.3",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  Compile / run / fork := true,
  Compile / run / connectInput := false
)

lazy val root = project.in(file(".")).aggregate(jvm).settings(
  name := "harmonia-off-ledger",
  publish / skip := true
)
