ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr.markdown_transformation")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.3.3"

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

Compile / compile / wartremoverErrors ++= Warts.unsafe.filter(
  !List(Wart.DefaultArguments, Wart.Throw).contains(_),
)
Test / compile / wartremoverErrors ++= Warts.unsafe.filter(_ != Wart.DefaultArguments)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Yexplicit-nulls", // https://docs.scala-lang.org/scala3/reference/other-new-features/explicit-nulls.html
  "-Ysafe-init", // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "markdown-transformation-kata-scala",
    Universal / maintainer := "https://eriktorr.es",
    Compile / mainClass := Some("es.eriktorr.markdown_transformation.MarkdownTransformationApp"),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.9.4",
      "co.fs2" %% "fs2-io" % "3.9.4",
      "com.github.scopt" %% "scopt" % "4.1.0",
      "io.monix" %% "newtypes-core" % "0.2.3",
      "org.apache.logging.log4j" % "log4j-api" % "2.23.1" % Runtime,
      "org.apache.logging.log4j" % "log4j-core" % "2.23.1" % Runtime,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.23.1" % Runtime,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test,
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-kernel" % "2.10.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "org.typelevel" %% "cats-effect-kernel" % "3.5.4",
      "org.typelevel" %% "cats-effect-std" % "3.5.4",
      "org.typelevel" %% "kittens" % "3.3.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "org.typelevel" %% "log4cats-core" % "2.6.0",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.typelevel" %% "scalacheck-effect" % "1.0.4" % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test,
    ),
    onLoadMessage := {
      s"""Custom tasks:
         |check - run all project checks
         |""".stripMargin
    },
  )

addCommandAlias(
  "check",
  "; undeclaredCompileDependenciesTest; unusedCompileDependenciesTest; scalafixAll; scalafmtSbtCheck; scalafmtCheckAll",
)
