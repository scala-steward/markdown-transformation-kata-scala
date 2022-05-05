package es.eriktorr.markdown_transformation

import cats.effect.IO

import scala.util.control.NoStackTrace

final case class MarkdownTransformationParams(
    inputFilename: String = "",
    outputFilename: String = "",
):
  def asString: String = s"input-filename=$inputFilename, output-filename=$outputFilename"

object MarkdownTransformationParams:
  def paramsFrom(args: List[String]): IO[MarkdownTransformationParams] =
    import scopt.OParser
    val builder = OParser.builder[MarkdownTransformationParams]
    val argParser =
      import builder.*
      OParser.sequence(
        programName("md-converter"),
        head("md-converter", "1.x"),
        opt[String]('i', "input")
          .required()
          .valueName("<file>")
          .action((x, c) => c.copy(inputFilename = x))
          .text("input is a required input filename"),
        opt[String]('o', "output")
          .required()
          .valueName("<file>")
          .action((x, c) => c.copy(outputFilename = x))
          .text("output is a required output filename"),
        help("help").text("prints this usage text"),
        checkConfig(c =>
          if c.inputFilename == c.outputFilename then
            failure("input and output files must be different")
          else success,
        ),
      )
    IO.fromOption(OParser.parse(argParser, args, MarkdownTransformationParams())) {
      Console.err.println(OParser.usage(argParser))
      IllegalArguments
    }

    // TODO: use validation

case object IllegalArguments extends NoStackTrace
