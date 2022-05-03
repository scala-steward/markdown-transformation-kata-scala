package es.eriktorr.markdown_transformation
package integration

import infrastructure.FileMarkdownReader
import infrastructure.MarkdownGenerators.textGen
import model.Line
import spec.MarkdownFileSpec

import cats.effect.{IO, Resource}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF
import org.scalacheck.effect.PropF.forAllF
import org.scalacheck.{Gen, Test}

import java.io.{File, FileWriter}
import java.nio.file.{Files, Path}

final class FileMarkdownReaderIntegrationTest extends MarkdownFileSpec("file-reader"):

  test("it should read a file line-by-line") {
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
