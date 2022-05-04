package es.eriktorr.markdown_transformation
package infrastructure

import cats.effect.{IO, Resource}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Test

import java.nio.file.{Files, Path}

abstract class MarkdownFileSuite(name: String) extends CatsEffectSuite with ScalaCheckEffectSuite:

  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  val temporaryFileFixture: Fixture[Path] =
    import scala.language.unsafeNulls
    val suiteName = s"md-converter-$name-integration-test"
    ResourceSuiteLocalFixture(
      s"$suiteName-temporary-file",
      Resource.make(IO.blocking(Files.createTempFile(suiteName, ".md")))(path =>
        IO.blocking(Files.deleteIfExists(path)),
      ),
    )

  override def munitFixtures: Seq[Fixture[?]] = List(temporaryFileFixture)
