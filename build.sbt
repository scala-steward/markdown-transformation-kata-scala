ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / idePackagePrefix := Some("es.eriktorr.markdown_transformation")
Global / excludeLintKeys += idePackagePrefix

ThisBuild / scalaVersion := "3.1.2"

Global / cancelable := true
Global / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges

Compile / compile / wartremoverErrors ++= Warts.unsafe
Test / compile / wartremoverErrors ++= Warts.unsafe

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
  //  "-rewrite",
  //  "-new-syntax" // https://docs.scala-lang.org/scala3/reference/other-new-features/control-syntax.html
  //  "-rewrite",
  //  "-indent" // https://docs.scala-lang.org/scala3/reference/other-new-features/indentation.html
)

lazy val root = (project in file("."))
  .settings(
    name := "markdown-transformation-kata-scala",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.2.7",
      "co.fs2" %% "fs2-io" % "3.2.7",
      "io.monix" %% "newtypes-core" % "0.2.1",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.typelevel" %% "kittens" % "3.0.0-M4",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.typelevel" %% "cats-effect" % "3.3.11",
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
