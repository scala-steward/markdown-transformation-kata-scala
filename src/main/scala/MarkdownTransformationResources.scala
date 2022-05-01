package es.eriktorr.markdown_transformation

import application.MarkdownTransformationUseCase
import infrastructure.{FileMarkdownReader, InMemoryLinksRepository, LinksRepositoryState}
import model.{LinksRepository, MarkdownReader}

import cats.effect.{IO, Ref, Resource}

final case class MarkdownTransformationResources(
    markdownReader: MarkdownReader,
    linksRepository: LinksRepository,
):
  val markdownTransformationUseCase: MarkdownTransformationUseCase =
    MarkdownTransformationUseCase(markdownReader, linksRepository)

object MarkdownTransformationResources:
  def impl(params: MarkdownTransformationParams): Resource[IO, MarkdownTransformationResources] =
    for
      linksRepositoryStateRef <- Resource.eval(
        Ref.of[IO, LinksRepositoryState](LinksRepositoryState(Map.empty)),
      )
      markdownReader <- Resource.eval(IO.delay(FileMarkdownReader(params.inputFilename)))
      linksRepository <- Resource.eval(IO.delay(InMemoryLinksRepository(linksRepositoryStateRef)))
    yield MarkdownTransformationResources(markdownReader, linksRepository)
