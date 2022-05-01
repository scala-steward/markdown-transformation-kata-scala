package es.eriktorr.markdown_transformation
package infrastructure

import model.{Line, MarkdownReader}

import cats.effect.{IO, Ref}
import fs2.Stream

final case class FakeMarkdownReaderState(lines: List[Line]):
  def set(newLines: List[Line]): FakeMarkdownReaderState = copy(lines = newLines)

object FakeMarkdownReaderState:
  def empty: FakeMarkdownReaderState = FakeMarkdownReaderState(List.empty)

final class FakeMarkdownReader(stateRef: Ref[IO, FakeMarkdownReaderState]) extends MarkdownReader:
  override def lines: Stream[IO, Line] = Stream.evals(stateRef.get.map(_.lines))
