package es.eriktorr.markdown_transformation

import infrastructure.{
  FakeMarkdownReader,
  FakeMarkdownReaderState,
  FootnotesRepositoryState,
  InMemoryFootnotesRepository,
}
import model.{Footnote, Line, Link, Reference}

import cats.effect.IO
import cats.effect.kernel.Ref

final case class MarkdownTransformationState(
    markdownReaderState: FakeMarkdownReaderState,
    footnotesRepositoryState: FootnotesRepositoryState,
):
  def setLines(lines: List[Line]): MarkdownTransformationState =
    copy(markdownReaderState = markdownReaderState.set(lines))

  def setFootnotes(footnotes: List[Footnote]): MarkdownTransformationState =
    copy(footnotesRepositoryState = footnotesRepositoryState.set(footnotes))

object MarkdownTransformationState:
  def empty: MarkdownTransformationState = MarkdownTransformationState(
    FakeMarkdownReaderState.empty,
    FootnotesRepositoryState.empty,
  )

object FakeMarkdownTransformationResources:
  def withResources[A](initialState: MarkdownTransformationState)(
      run: MarkdownTransformationResources => IO[A],
  ): IO[(Either[Throwable, A], MarkdownTransformationState)] = for
    markdownReaderStateRef <- Ref.of[IO, FakeMarkdownReaderState](initialState.markdownReaderState)
    footnotesRepositoryStateRef <- Ref.of[IO, FootnotesRepositoryState](
      initialState.footnotesRepositoryState,
    )
    resources = MarkdownTransformationResources(
      FakeMarkdownReader(markdownReaderStateRef),
      InMemoryFootnotesRepository(footnotesRepositoryStateRef),
    )
    result <- run(resources).attempt
    finalMarkdownReaderState <- markdownReaderStateRef.get
    finalFootnotesRepositoryState <- footnotesRepositoryStateRef.get
    finalState = initialState.copy(
      markdownReaderState = finalMarkdownReaderState,
      footnotesRepositoryState = finalFootnotesRepositoryState,
    )
  yield (result, finalState)
