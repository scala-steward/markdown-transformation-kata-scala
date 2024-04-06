package es.eriktorr.markdown_transformation
package infrastructure

import infrastructure.MarkdownGenerators.Fragment.{ImageFragment, LinkFragment, TextFragment}
import model.{Line, Link, LinkText, LinkUrl}

import org.scalacheck.Gen

import scala.util.Random

object MarkdownGenerators:
  val imageGen: Gen[Image] =
    for
      imageCaption <- textGen()
      imageUrl <- urlGen
    yield Image(ImageCaption(imageCaption), ImageUrl(imageUrl))

  val linkGen: Gen[Link] =
    for
      linkText <- textGen()
      linkUrl <- urlGen
    yield Link(LinkText(linkText), LinkUrl(linkUrl))

  def fragmentsGen(links: List[Link]): Gen[List[Fragment]] = for
    numberOfImages <- Gen.choose(0, 2)
    imageFragments <- Gen.listOfN[Image](numberOfImages, imageGen).map(_.map(ImageFragment(_)))
    numberOfLinks <- Gen.choose(0, 4)
    linkFragments <- Gen.listOfN[Link](numberOfLinks, Gen.oneOf(links)).map(_.map(LinkFragment(_)))
    numberOfTexts <- Gen.choose(0, 12)
    textFragments <- Gen.listOfN[String](numberOfTexts, textGen()).map(_.map(TextFragment(_)))
  yield Random.shuffle(imageFragments ++ linkFragments ++ textFragments)

  val linesGen: Gen[List[Line]] =
    Gen.listOf(Gen.frequency(7 -> textGen(1, 100).map(Line(_)), 3 -> Gen.const(Line.empty)))

  /** @see
    *   [[https://medium.com/@supermanue/building-useful-scalacheck-generators-71635d1edb9d Building useful Scalacheck Generators]]
    */
  private def urlGen: Gen[String] =
    def httpTypeGen: Gen[String] = Gen.oneOf(Seq("http", "https"))
    def domainGen: Gen[String] = for
      numberOfFragments <- Gen.choose(1, 3)
      domain <- Gen.listOfN[String](numberOfFragments, textGen()).map(_.mkString("."))
    yield domain
    def domainTypeGen: Gen[String] = Gen.oneOf(Seq("com", "es", "org"))
    def pathGen: Gen[String] = for
      numberOfFragments <- Gen.choose(1, 3)
      path <- Gen.listOfN[String](numberOfFragments, textGen()).map(_.mkString("/"))
    yield path
    for
      http <- httpTypeGen
      domain <- domainGen
      domainType <- domainTypeGen
      path <- pathGen
    yield http + "//" + domain + "." + domainType + "/" + path

  def textGen(minLength: Int = 3, maxLength: Int = 10): Gen[String] = for
    length <- Gen.choose(minLength, maxLength)
    text <- Gen.listOfN[Char](length, Gen.alphaNumChar).map(_.mkString)
  yield text

  sealed trait Fragment

  object Fragment:
    final case class ImageFragment(image: Image) extends Fragment
    final case class LinkFragment(link: Link) extends Fragment
    final case class TextFragment(text: String) extends Fragment
