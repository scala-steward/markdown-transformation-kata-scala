package es.eriktorr.markdown_transformation
package model

import cats.effect.IO
import fs2.Stream

trait MarkdownWriter:
  def write(lines: Stream[IO, Line]): Stream[IO, Unit]
