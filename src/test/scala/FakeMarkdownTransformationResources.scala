package es.eriktorr.markdown_transformation

import infrastructure.{
  FakeMarkdownReader,
  FakeMarkdownReaderState,
  FakeMarkdownWriter,
  FakeMarkdownWriterState,
  FootnotesRepositoryState,
  InMemoryFootnotesRepository,
}
import model.{Footnote, Line, Link, Reference}

import cats.effect.IO
import cats.effect.kernel.Ref

final case class MarkdownTransformationState(
    markdownReaderState: FakeMarkdownReaderState,
    markdownWriterState: FakeMarkdownWriterState,
    footnotesRepositoryState: FootnotesRepositoryState,
):
  def setReaderLines(lines: List[Line]): MarkdownTransformationState =
    copy(markdownReaderState = markdownReaderState.set(lines))

  def setWriterLines(lines: List[Line]): MarkdownTransformationState =
    copy(markdownWriterState = markdownWriterState.set(lines))

  def setFootnotes(footnotes: List[Footnote]): MarkdownTransformationState =
    copy(footnotesRepositoryState = footnotesRepositoryState.set(footnotes))

object MarkdownTransformationState:
  def empty: MarkdownTransformationState = MarkdownTransformationState(
    FakeMarkdownReaderState.empty,
    FakeMarkdownWriterState.empty,
    FootnotesRepositoryState.empty,
  )

object FakeMarkdownTransformationResources:
  def withResources[A](initialState: MarkdownTransformationState)(
      run: MarkdownTransformationResources => IO[A],
  ): IO[(Either[Throwable, A], MarkdownTransformationState)] = for
    markdownReaderStateRef <- Ref.of[IO, FakeMarkdownReaderState](initialState.markdownReaderState)
    markdownWriterStateRef <- Ref.of[IO, FakeMarkdownWriterState](initialState.markdownWriterState)
    footnotesRepositoryStateRef <- Ref.of[IO, FootnotesRepositoryState](
      initialState.footnotesRepositoryState,
    )
    resources = MarkdownTransformationResources(
      FakeMarkdownReader(markdownReaderStateRef),
      FakeMarkdownWriter(markdownWriterStateRef),
      InMemoryFootnotesRepository(footnotesRepositoryStateRef),
    )
    result <- run(resources).attempt
    finalMarkdownReaderState <- markdownReaderStateRef.get
    finalMarkdownWriterState <- markdownWriterStateRef.get
    finalFootnotesRepositoryState <- footnotesRepositoryStateRef.get
    finalState = initialState.copy(
      markdownReaderState = finalMarkdownReaderState,
      markdownWriterState = finalMarkdownWriterState,
      footnotesRepositoryState = finalFootnotesRepositoryState,
    )
  yield (result, finalState)
