package es.eriktorr.markdown_transformation
package infrastructure

import model.{Line, MarkdownWriter}

import cats.effect.{IO, Ref}
import fs2.Stream

final case class FakeMarkdownWriterState(lines: List[Line]):
  def set(newLines: List[Line]): FakeMarkdownWriterState = copy(lines = newLines)

object FakeMarkdownWriterState:
  def empty: FakeMarkdownWriterState = FakeMarkdownWriterState(List.empty)

final class FakeMarkdownWriter(stateRef: Ref[IO, FakeMarkdownWriterState]) extends MarkdownWriter:
  override def write(lines: Stream[IO, Line]): Stream[IO, Unit] =
    lines
      .fold(List.empty[Line])((xs, x) => x :: xs)
      .map(_.reverse)
      .evalMap(xs => stateRef.update(currentState => currentState.copy(xs)))
