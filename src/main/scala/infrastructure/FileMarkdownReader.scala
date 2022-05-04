package es.eriktorr.markdown_transformation
package infrastructure

import model.{Line, MarkdownReader}

import cats.effect.IO
import fs2.io.file.{Files, Path}
import fs2.{text, Stream}

final class FileMarkdownReader(filename: String) extends MarkdownReader:
  override def lines: Stream[IO, Line] =
    Files[IO].readAll(Path(filename)).through(text.utf8.decode).through(text.lines).map(Line(_))
