package es.eriktorr.markdown_transformation

import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object MarkdownTransformationApp extends IOApp:
  private[this] def program(params: MarkdownTransformationParams) =
    MarkdownTransformationResources.impl(params).use(_.markdownTransformationUseCase.run)

  override def run(args: List[String]): IO[ExitCode] = (for
    logger <- Slf4jLogger.create[IO]
    params <- MarkdownTransformationParams.paramsFrom(args)
    _ <- logger.info(s"Running with parameters: ${params.asString}")
    result <- program(params)
    _ <- logger.info("Done!")
  yield result).as(ExitCode.Success)
