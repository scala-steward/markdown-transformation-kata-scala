package es.eriktorr.markdown_transformation
package application

import application.MarkdownTransformationUseCase.linkPattern
import model.{Footnote, *}

import cats.effect.IO
import cats.implicits.*
import fs2.{Chunk, Stream}

import scala.collection.immutable.List

final class MarkdownTransformationUseCase(
    markdownReader: MarkdownReader,
    markdownWriter: MarkdownWriter,
    footnotesRepository: FootnotesRepository,
):
  def run: IO[Unit] =
    (markdownReader.lines.flatMap(transform) ++ format(footnotesRepository.footnotes))
      .through(markdownWriter.write)
      .compile
      .drain

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  private[this] def transform(line: Line) = Stream
    .emit(
      linkPattern.replaceAllIn(
        line.value,
        matcher =>
          import cats.effect.unsafe.implicits.global
          val link = Link(LinkText(matcher.group("text")), LinkUrl(matcher.group("url")))
          footnotesRepository.save(link).attempt.unsafeRunSync() match
            case Right(reference) => s"${link.text} [^${reference.value}]"
            case Left(error) => throw error,
      ),
    )
    .map(Line(_))

  private[this] def transform2(line: Line) =
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

  private[this] def format(footnotes: IO[List[Footnote]]) = Stream
    .evals(footnotes.map(_.sorted))
    .map { case Footnote(reference, link) =>
      Line(s"[^${reference.value}]: ${link.url.value}")
    }

object MarkdownTransformationUseCase:
  /** @see
    *   [[https://www.regular-expressions.info/lookaround.html Regular expressions lookaround]]
    */
  private val linkPattern = raw"(?<!!)\[(?<text>[^\^].+?)]\((?<url>.+?)\)".r.unanchored
