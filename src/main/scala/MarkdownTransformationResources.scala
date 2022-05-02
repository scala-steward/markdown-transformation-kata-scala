package es.eriktorr.markdown_transformation

import application.MarkdownTransformationUseCase
import infrastructure.{
  FileMarkdownReader,
  FileMarkdownWriter,
  FootnotesRepositoryState,
  InMemoryFootnotesRepository,
}
import model.{FootnotesRepository, MarkdownReader, MarkdownWriter}

import cats.effect.{IO, Ref, Resource}

final case class MarkdownTransformationResources(
    markdownReader: MarkdownReader,
    markdownWriter: MarkdownWriter,
    footnotesRepository: FootnotesRepository,
):
  val markdownTransformationUseCase: MarkdownTransformationUseCase =
    MarkdownTransformationUseCase(markdownReader, markdownWriter, footnotesRepository)

object MarkdownTransformationResources:
  def impl(params: MarkdownTransformationParams): Resource[IO, MarkdownTransformationResources] =
    for
      footnotesRepositoryStateRef <- Resource.eval(
        Ref.of[IO, FootnotesRepositoryState](FootnotesRepositoryState.empty),
      )
      markdownReader <- Resource.eval(IO.delay(FileMarkdownReader(params.inputFilename)))
      markdownWriter <- Resource.eval(IO.delay(FileMarkdownWriter(params.outputFilename)))
      footnotesRepository <- Resource.eval(
        IO.delay(InMemoryFootnotesRepository(footnotesRepositoryStateRef)),
      )
    yield MarkdownTransformationResources(markdownReader, markdownWriter, footnotesRepository)
