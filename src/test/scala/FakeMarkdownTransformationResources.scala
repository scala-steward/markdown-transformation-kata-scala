package es.eriktorr.markdown_transformation

import infrastructure.{
  FakeMarkdownReader,
  FakeMarkdownReaderState,
  InMemoryLinksRepository,
  LinksRepositoryState,
}
import model.Line

import cats.effect.IO
import cats.effect.kernel.Ref

final case class MarkdownTransformationState(
    markdownReaderState: FakeMarkdownReaderState,
    linksRepositoryState: LinksRepositoryState,
):
  def set(lines: List[Line]): MarkdownTransformationState =
    copy(markdownReaderState = markdownReaderState.set(lines))

object MarkdownTransformationState:
  def empty: MarkdownTransformationState = MarkdownTransformationState(
    FakeMarkdownReaderState.empty,
    LinksRepositoryState.empty,
  )

object FakeMarkdownTransformationResources:
  def withResources[A](initialState: MarkdownTransformationState)(
      run: MarkdownTransformationResources => IO[A],
  ): IO[(Either[Throwable, A], MarkdownTransformationState)] = for
    markdownReaderStateRef <- Ref.of[IO, FakeMarkdownReaderState](initialState.markdownReaderState)
    linksRepositoryStateRef <- Ref.of[IO, LinksRepositoryState](initialState.linksRepositoryState)
    resources = MarkdownTransformationResources(
      FakeMarkdownReader(markdownReaderStateRef),
      InMemoryLinksRepository(linksRepositoryStateRef),
    )
    result <- run(resources).attempt
    finalMarkdownReaderState <- markdownReaderStateRef.get
    finalLinksRepositoryState <- linksRepositoryStateRef.get
    finalState = initialState.copy(
      markdownReaderState = finalMarkdownReaderState,
      linksRepositoryState = finalLinksRepositoryState,
    )
  yield (result, finalState)
