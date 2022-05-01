package es.eriktorr.markdown_transformation
package application

import application.MarkdownTransformationUseCase.linkPattern
import model.{Line, Link, LinksRepository, LinkText, LinkUrl, MarkdownReader}

import cats.implicits.*
import cats.effect.IO
import fs2.{Chunk, Stream}

final class MarkdownTransformationUseCase(
    markdownReader: MarkdownReader,
    linksRepository: LinksRepository,
):
  def run: IO[Unit] = for
    _ <- markdownReader.lines
      .evalTap(line => IO.delay(println(s"${line.value}"))) // TODO
      .map(transform(_).lastOr(Line.empty))
      .compile
      .drain
  yield ()

  private[this] def transform(line: Line) =
    Stream.unfoldChunkEval[IO, Option[String], Line](Some(line.value)) {
      case Some(text) =>
        (text match
          case linkPattern(text, url) => Some(Link(LinkText(text), LinkUrl(url)))
          case _ => Option.empty
        ).fold(IO.delay(Some((Chunk(Line(text)), Option.empty)))) { link =>
          for
            reference <- linksRepository.save(link)
            transformedText = linkPattern.replaceFirstIn(
              text,
              s"${link.text} [^${reference.value}]",
            )
          yield Some((Chunk(Line(transformedText)), Some(transformedText)))
        }
      case None => IO.none
    }

object MarkdownTransformationUseCase:
  private val linkPattern = "\\[(.+?)]\\((.+?)\\)".r.unanchored
