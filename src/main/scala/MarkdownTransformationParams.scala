package es.eriktorr.markdown_transformation

final case class MarkdownTransformationParams(
    inputFilename: String = "",
    outputFilename: String = "",
):
  def asString: String = s"input-filename=$inputFilename, output-filename=$outputFilename"

object MarkdownTransformationParams:
  import scopt.OParser

  private[this] val builder = OParser.builder[MarkdownTransformationParams]
  private[this] val argParser =
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

  def paramsFrom(args: List[String]): Option[MarkdownTransformationParams] =
    OParser.parse(argParser, args, MarkdownTransformationParams())

  def usage: String = OParser.usage(argParser)
