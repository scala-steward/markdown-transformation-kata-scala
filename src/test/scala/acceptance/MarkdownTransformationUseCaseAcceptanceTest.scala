package es.eriktorr.markdown_transformation
package acceptance

import acceptance.MarkdownTransformationUseCaseAcceptanceTest.{tesCaseGen, TestCase}
import infrastructure.MarkdownGenerators.Fragment.{ImageFragment, LinkFragment, TextFragment}
import infrastructure.MarkdownGenerators.{fragmentsGen, linkGen, Fragment}
import model.{Footnote, Line, Link, Reference}

import cats.effect.IO
import cats.implicits.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

import scala.annotation.tailrec

final class MarkdownTransformationUseCaseAcceptanceTest
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  test("it should transform a markdown document") {
    forAllF(tesCaseGen) { testCase =>
      FakeMarkdownTransformationResources
        .withResources(testCase.initialState)(_.markdownTransformationUseCase.run)
        .map { case (result, finalState) =>
          assert(result.isRight)
          assertEquals(finalState, testCase.expectedState)
        }
    }
  }

object MarkdownTransformationUseCaseAcceptanceTest:
  final private case class TestCase(
      initialState: MarkdownTransformationState,
      expectedState: MarkdownTransformationState,
  )

  private[this] def footnotesFrom(lineFragments: List[List[Fragment]]) =
    @tailrec
    def unique(links: List[Link], linksAcc: List[Link]): List[Link] = links match
      case Nil => linksAcc
      case ::(head, next) =>
        unique(next, if linksAcc.contains(head) then linksAcc else head :: linksAcc)

    unique(
      lineFragments.flatMap(_.collect { case LinkFragment(link) => link }),
      List.empty,
    ).reverse.zipWithIndex.map { case (link, index) => Footnote(Reference(index + 1), link) }

  private[this] def linesFrom(lineFragments: List[List[Fragment]]) = lineFragments
    .map(_.map {
      _ match
        case ImageFragment(image) => s"![${image.caption}](${image.url})"
        case LinkFragment(link) => s"[${link.text}](${link.url})"
        case TextFragment(text) => text
    })
    .map(_.mkString(" "))
    .map(Line(_))

  private[this] def transformedLinesFrom(
      lineFragments: List[List[Fragment]],
      footnotes: List[Footnote],
  ) =
    (lineFragments
      .map(_.map {
        _ match
          case ImageFragment(image) => s"![${image.caption}](${image.url})"
          case LinkFragment(link) =>
            footnotes
              .find(_.link === link)
              .fold("")(x => s"${x.link.text} [^${x.reference.value}]")
          case TextFragment(text) => text
      })
      .map(_.mkString(" ")) ++ footnotes.map(x => s"[^${x.reference.value}]: ${x.link.url}"))
      .map(Line(_))

  private val tesCaseGen: Gen[TestCase] = for
    links <- Gen.nonEmptyContainerOf[Set, Link](linkGen).map(_.toList)
    numberOfLines <- Gen.choose(0, 5)
    lineFragments <- Gen.listOfN[List[Fragment]](numberOfLines, fragmentsGen(links))
    footnotes = footnotesFrom(lineFragments)
    initialState = MarkdownTransformationState.empty.setReaderLines(linesFrom(lineFragments))
    expectedState = initialState
      .setFootnotes(footnotes.reverse)
      .setWriterLines(transformedLinesFrom(lineFragments, footnotes))
  yield TestCase(initialState, expectedState)
