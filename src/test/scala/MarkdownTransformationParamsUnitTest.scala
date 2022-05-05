package es.eriktorr.markdown_transformation

import MarkdownTransformationParamsUnitTest.{tesCaseGen, TestCase}
import infrastructure.MarkdownGenerators.textGen

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import scala.util.Random

final class MarkdownTransformationParamsUnitTest extends ScalaCheckSuite:

  property("parse program arguments") {
    forAll(tesCaseGen) { case TestCase(args, expectedParams) =>
      assertEquals(MarkdownTransformationParams.paramsFrom(args), Some(expectedParams))
    }
  }

  test("fail when input filename is missing") {
    assertEquals(MarkdownTransformationParams.paramsFrom(List("-o example.md")), Option.empty)
  }

  test("fail when output filename is missing") {
    assertEquals(MarkdownTransformationParams.paramsFrom(List("-i example.md")), Option.empty)
  }

  test("fail when both input and output filenames are the same") {
    assertEquals(
      MarkdownTransformationParams.paramsFrom(List("-i example.md", "-o example.md")),
      Option.empty,
    )
  }

object MarkdownTransformationParamsUnitTest:
  final private case class TestCase(
      args: List[String],
      expectedParams: MarkdownTransformationParams,
  )

  private val tesCaseGen: Gen[TestCase] = for
    inputFilename :: outputFilename :: Nil <- Gen
      .containerOfN[Set, String](2, textGen())
      .map(_.toList)
    inputArgument <- Gen.oneOf("-i", "--input")
    outputArgument <- Gen.oneOf("-o", "--output")
  yield
    import scala.language.unsafeNulls
    TestCase(
      Random
        .shuffle(List(s"$inputArgument $inputFilename", s"$outputArgument $outputFilename"))
        .flatMap(_.split(" ")),
      MarkdownTransformationParams(inputFilename, outputFilename),
    )
