package es.eriktorr.markdown_transformation
package integration

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite

import java.nio.file.{Files, Path}

final class FileMarkdownReaderIntegrationTest extends CatsEffectSuite:

  val temporaryFileFixture: Fixture[Path] =
    import scala.language.unsafeNulls
    ResourceSuiteLocalFixture(
      "temporary-file",
      Resource.make(IO.blocking(Files.createTempFile("md-converter-test-", ".md")))(path =>
        IO.blocking(Files.deleteIfExists(path)),
      ),
    )

  override def munitFixtures: Seq[Fixture[?]] = List(temporaryFileFixture)

  test("TODO") {
    fail("not implemented")
  }
