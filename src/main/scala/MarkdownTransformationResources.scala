package es.eriktorr.markdown_transformation

import application.MarkdownTransformationUseCase
import infrastructure.{FileMarkdownReader, FootnotesRepositoryState, InMemoryFootnotesRepository}
import model.{FootnotesRepository, MarkdownReader}

import cats.effect.{IO, Ref, Resource}

final case class MarkdownTransformationResources(
    markdownReader: MarkdownReader,
    footnotesRepository: FootnotesRepository,
):
  val markdownTransformationUseCase: MarkdownTransformationUseCase =
    MarkdownTransformationUseCase(markdownReader, footnotesRepository)

object MarkdownTransformationResources:
  def impl(params: MarkdownTransformationParams): Resource[IO, MarkdownTransformationResources] =
    for
      footnotesRepositoryStateRef <- Resource.eval(
        Ref.of[IO, FootnotesRepositoryState](FootnotesRepositoryState.empty),
      )
      markdownReader <- Resource.eval(IO.delay(FileMarkdownReader(params.inputFilename)))
      linksRepository <- Resource.eval(
        IO.delay(InMemoryFootnotesRepository(footnotesRepositoryStateRef)),
      )
    yield MarkdownTransformationResources(markdownReader, linksRepository)
