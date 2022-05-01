package es.eriktorr.markdown_transformation
package model

import cats.effect.IO
import fs2.Stream

trait MarkdownReader:
  def lines: Stream[IO, Line]
