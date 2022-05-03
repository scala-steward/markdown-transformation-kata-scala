package es.eriktorr.markdown_transformation
package integration

import infrastructure.FileMarkdownWriter
import infrastructure.MarkdownGenerators.textGen
import model.Line
import spec.MarkdownFileSpec

import cats.effect.IO
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF
import fs2.Stream

import scala.io.Source

final class FileMarkdownWriterIntegrationTest extends MarkdownFileSpec("file-writer"):

  test("it should write a file line-by-line") {
    val linesGen =
      Gen.listOf(Gen.frequency(7 -> textGen(1, 100).map(Line(_)), 3 -> Gen.const(Line.empty)))

    forAllF(linesGen) { lines =>
      for
        fileName <- IO.delay(temporaryFileFixture().toString)
        _ <- FileMarkdownWriter(fileName).write(Stream.evals(IO.delay(lines))).compile.drain
        result <- IO.blocking {
          val bufferedSource = Source.fromFile(fileName)
          val result = bufferedSource.getLines.map(Line(_)).toList
          bufferedSource.close
          result
        }
      yield assertEquals(result, lines)
    }
  }
