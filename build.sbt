ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr.markdown_transformation")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.1.2"

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

Compile / compile / wartremoverErrors ++= Warts.unsafe.filter(_ != Wart.DefaultArguments)
Test / compile / wartremoverErrors ++= Warts.unsafe.filter(_ != Wart.DefaultArguments)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

/** So to go from old-style syntax to new-style indented code one has to invoke the compiler twice,
  * first with options `-rewrite -new-syntax`, then again with options `-rewrite -indent`.
  */
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
    Compile / mainClass := Some("es.eriktorr.markdown_transformation.MarkdownTransformationApp"),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.2.7",
      "co.fs2" %% "fs2-io" % "3.2.7",
      "com.github.scopt" %% "scopt" % "4.0.1",
      "io.monix" %% "newtypes-core" % "0.2.1",
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.2" % Runtime,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test,
      "org.typelevel" %% "cats-core" % "2.7.0",
      "org.typelevel" %% "cats-kernel" % "2.7.0",
      "org.typelevel" %% "cats-effect" % "3.3.11",
      "org.typelevel" %% "cats-effect-kernel" % "3.3.11",
      "org.typelevel" %% "kittens" % "3.0.0-M4",
      "org.typelevel" %% "log4cats-slf4j" % "2.3.0",
      "org.typelevel" %% "log4cats-core_sjs1" % "2.3.0",
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
