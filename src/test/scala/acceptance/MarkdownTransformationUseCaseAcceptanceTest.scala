package es.eriktorr.markdown_transformation
package acceptance

import acceptance.MarkdownTransformationUseCaseAcceptanceTest.TestCase
import infrastructure.MarkdownGenerators.{lineGen, linkGen}
import model.{Footnote, Line, Link, Reference}

import cats.effect.IO
import es.eriktorr.markdown_transformation.FakeMarkdownTransformationResources.withResources
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class MarkdownTransformationUseCaseAcceptanceTest
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  test("it should transform a markdown document") {
    val tesCaseGen = for
      links <- Gen.containerOf[Set, Link](linkGen).map(_.toList)
      footnotes = links.zipWithIndex.map { case (link, index) =>
        Footnote(Reference(index + 1), link)
      }
      numberOfLines <- Gen.choose(0, 5)
      lines <- Gen.listOfN[Line](numberOfLines, lineGen(links))
      initialState = MarkdownTransformationState.empty.setLines(lines)
      expectedState = initialState.setFootnotes(footnotes)
    yield TestCase(initialState, expectedState)

    forAllF(tesCaseGen) { testCase =>
      withResources(testCase.initialState)(_.markdownTransformationUseCase.run)
        .map { case (result, finalState) =>
          assert(result.isRight)
          assertEquals(finalState, testCase.expectedState)
        }
    }
  }

object MarkdownTransformationUseCaseAcceptanceTest:
  final case class TestCase(
      initialState: MarkdownTransformationState,
      expectedState: MarkdownTransformationState,
  )
