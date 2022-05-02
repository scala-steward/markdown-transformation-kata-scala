package es.eriktorr.markdown_transformation
package infrastructure

import model.{Line, MarkdownWriter}

import cats.effect.IO
import fs2.io.file.{Files, Path}
import fs2.text.utf8
import fs2.{text, Stream}

final class FileMarkdownWriter(filename: String) extends MarkdownWriter:
  override def write(lines: Stream[IO, Line]): Stream[IO, Unit] = lines
    .map(_.value)
    .intersperse("\n")
    .through(text.utf8.encode)
    .through(Files[IO].writeAll(Path(filename)))
