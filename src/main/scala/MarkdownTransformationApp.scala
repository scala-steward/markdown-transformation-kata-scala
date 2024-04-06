package es.eriktorr.markdown_transformation

import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object MarkdownTransformationApp extends IOApp:
  private def program(params: MarkdownTransformationParams) =
    MarkdownTransformationResources.impl(params).use(_.markdownTransformationUseCase.run)

  override def run(args: List[String]): IO[ExitCode] =
    MarkdownTransformationParams.paramsFrom(args) match
      case Some(params) =>
        for
          logger <- Slf4jLogger.create[IO]
          _ <- logger.info(s"Running with parameters: ${params.asString}")
          _ <- program(params)
          _ <- logger.info("Done!")
        yield ExitCode.Success
      case None => Console[IO].errorln(MarkdownTransformationParams.usage).as(ExitCode.Error)
