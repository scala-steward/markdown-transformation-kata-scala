package es.eriktorr.markdown_transformation
package application

import application.MarkdownTransformationUseCase.linkPattern
import model.{Footnote, *}

import cats.effect.IO
import fs2.Stream

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
          val link = Link(LinkText(matcher.group("text")), LinkUrl(matcher.group("url")))
          import cats.effect.unsafe.implicits.global
          footnotesRepository.save(link).attempt.unsafeRunSync() match
            case Right(reference) => s"${link.text} [^${reference.value}]"
            case Left(error) => throw error,
      ),
    )
    .map(Line(_))

  private[this] def format(footnotes: IO[List[Footnote]]) = Stream
    .evals(footnotes.map(_.sorted))
    .map { case Footnote(reference, link) =>
      Line(s"[^${reference.value}]: ${link.url.value}")
    }

object MarkdownTransformationUseCase:
  /** @see
    *   [[https://www.regular-expressions.info/lookaround.html Lookaround]]
    * @see
    *   [[https://www.regular-expressions.info/refext.html Named Groups]]
    */
  private val linkPattern = raw"(?<!!)\[(?<text>[^\^].+?)]\((?<url>.+?)\)".r.unanchored
