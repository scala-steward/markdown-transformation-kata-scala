package es.eriktorr.markdown_transformation
package integration

import infrastructure.{FileMarkdownWriter, MarkdownFileSuite}
import infrastructure.MarkdownGenerators.linesGen
import model.Line

import cats.effect.IO
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF
import fs2.Stream

import scala.io.Source

final class FileMarkdownWriterIntegrationTest extends MarkdownFileSuite("file-writer"):

  test("it should write a file line-by-line") {
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
