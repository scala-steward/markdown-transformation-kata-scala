package es.eriktorr.markdown_transformation

import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object MarkdownTransformationApp extends IOApp:
  override def run(args: List[String]): IO[ExitCode] = (for _ <- Slf4jLogger.create[IO]
  yield ()).as(ExitCode.Success)
