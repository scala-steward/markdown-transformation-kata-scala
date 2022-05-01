package es.eriktorr.markdown_transformation
package acceptance

import acceptance.MarkdownTransformationUseCaseAcceptanceTest.TestCase
import infrastructure.MarkdownGenerators.{lineGen, linkGen}
import model.{Line, Link, Reference}

import cats.effect.IO
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF

final class MarkdownTransformationUseCaseAcceptanceTest
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  test("it should transform a markdown document") {
    val tesCaseGen = for
      links <- Gen.containerOf[Set, Link](linkGen)
      linksWithReference = links.zipWithIndex.map { case (link, index) =>
        link -> Reference(index + 1)
      }.toMap
      numberOfLines <- Gen.choose(0, 5)
      lines <- Gen.listOfN[Line](numberOfLines, lineGen())
      initialState = MarkdownTransformationState.empty.set(lines)
      expectedState = initialState.set(linksWithReference)
    yield TestCase(initialState, expectedState)

    PropF.forAllF(tesCaseGen) { testCase =>
      FakeMarkdownTransformationResources
        .withResources(testCase.initialState)(
          _.markdownTransformationUseCase.run,
        )
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
