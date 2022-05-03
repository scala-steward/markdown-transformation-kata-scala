package es.eriktorr.markdown_transformation
package acceptance

import acceptance.MarkdownTransformationUseCaseAcceptanceTest.TestCase
import infrastructure.MarkdownGenerators.Fragment.{LinkFragment, TextFragment}
import infrastructure.MarkdownGenerators.{fragmentsGen, linkGen, Fragment}
import model.{Footnote, Line, Link, Reference}

import cats.effect.IO
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class MarkdownTransformationUseCaseAcceptanceTest
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  test("it should transform a markdown document") {
    val tesCaseGen = for
      links <- Gen.nonEmptyContainerOf[Set, Link](linkGen).map(_.toList)
      numberOfLines <- Gen.choose(0, 5)
      lineFragments <- Gen.listOfN[List[Fragment]](numberOfLines, fragmentsGen(links))
      footnotes = lineFragments
        .flatMap(_.collect { case LinkFragment(link) => link })
        .toSet
        .zipWithIndex
        .map { case (link, index) => Footnote(Reference(index + 1), link) }
        .toList
        .sorted
      initialState = MarkdownTransformationState.empty.setReaderLines(
        lineFragments
          .map(_.map {
            _ match
              case LinkFragment(link) => s"[${link.text}](${link.url})"
              case TextFragment(text) => text
          })
          .map(_.mkString(" "))
          .map(Line(_)),
      )
      expectedState = initialState
        .setFootnotes(footnotes.reverse)
        .setWriterLines(
          (lineFragments
            .map(_.map {
              _ match
                case LinkFragment(link) =>
                  footnotes
                    .find(_.link == link)
                    .fold("")(x => s"${x.link.text} [^${x.reference.value}]")
                case TextFragment(text) => text
            })
            .map(_.mkString(" ")) ++ footnotes
            .map(x => s"[^${x.reference.value}]: ${x.link.url}")).map(Line(_)),
        )
    yield TestCase(initialState, expectedState)

    forAllF(tesCaseGen) { testCase =>
      FakeMarkdownTransformationResources
        .withResources(testCase.initialState)(_.markdownTransformationUseCase.run)
        .map { case (result, finalState) =>
          // TODO
          println(s"\n\n >> RESULT: ${finalState.footnotesRepositoryState.footnotes}\n")
          println(s"\n\n >> EXPECT: ${testCase.expectedState.footnotesRepositoryState.footnotes}\n")
          // TODO
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
