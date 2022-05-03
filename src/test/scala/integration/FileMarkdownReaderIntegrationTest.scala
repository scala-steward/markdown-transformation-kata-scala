package es.eriktorr.markdown_transformation
package integration

import infrastructure.FileMarkdownReader
import infrastructure.MarkdownGenerators.textGen
import model.Line

import cats.effect.{IO, Resource}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF
import org.scalacheck.effect.PropF.forAllF
import org.scalacheck.{Gen, Test}

import java.io.{File, FileWriter}
import java.nio.file.{Files, Path}

final class FileMarkdownReaderIntegrationTest extends CatsEffectSuite with ScalaCheckEffectSuite:

  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  val temporaryFileFixture: Fixture[Path] =
    import scala.language.unsafeNulls
    val suiteName = "md-converter-file-reader-integration-test"
    ResourceSuiteLocalFixture(
      s"$suiteName-temporary-file",
      Resource.make(IO.blocking(Files.createTempFile(suiteName, ".md")))(path =>
        IO.blocking(Files.deleteIfExists(path)),
      ),
    )

  override def munitFixtures: Seq[Fixture[?]] = List(temporaryFileFixture)

  test("it should read a file") {
    val linesGen =
      Gen.listOf(Gen.frequency(7 -> textGen(1, 100).map(Line(_)), 3 -> Gen.const(Line.empty)))

    forAllF(linesGen) { lines =>
      for
        fileName <- IO.delay(temporaryFileFixture().toString)
        _ <- IO.blocking {
          val fileWriter = FileWriter(File(fileName))
          lines.foreach(line => fileWriter.write(line.value))
          fileWriter.close()
        }
        result <- FileMarkdownReader(fileName).lines.compile.toList
      yield assertEquals(result, lines)
    }
  }
