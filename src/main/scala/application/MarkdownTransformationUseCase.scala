package es.eriktorr.markdown_transformation
package application

import application.MarkdownTransformationUseCase.linkPattern
import model.*

import cats.effect.IO
import cats.implicits.*
import fs2.{Chunk, Stream}

final class MarkdownTransformationUseCase(
    markdownReader: MarkdownReader,
    markdownWriter: MarkdownWriter,
    footnotesRepository: FootnotesRepository,
):
  def run: IO[Unit] = for
    _ <- (markdownReader.lines.flatMap(transform) ++ Stream
      .evals(footnotesRepository.footnotes.map(_.sorted))
      .map { case Footnote(reference, link) =>
        Line(s"[^${reference.value}]: ${link.url.value}")
      }).through(markdownWriter.write).compile.drain
  yield ()

  private[this] def transform(line: Line) =
    Stream
      .unfoldChunkEval[IO, Option[String], Line](Some(line.value)) {
        case Some(text) =>
          (text match
            case linkPattern(text, url) => Some(Link(LinkText(text), LinkUrl(url)))
            case _ => Option.empty
          ).fold(IO.delay(Some((Chunk(Line(text)), Option.empty)))) { link =>
            for
              reference <- footnotesRepository.save(link)
              transformedText = linkPattern.replaceFirstIn(
                text,
                s"${link.text} [^${reference.value}]",
              )
            yield Some((Chunk(Line(transformedText)), Some(transformedText)))
          }
        case None => IO.none
      }
      .lastOr(Line.empty)

object MarkdownTransformationUseCase:
  private val linkPattern = "\\[([^\\^].+?)]\\((.+?)\\)".r.unanchored
