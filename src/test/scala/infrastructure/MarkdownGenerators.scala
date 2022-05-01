package es.eriktorr.markdown_transformation
package infrastructure

import model.{Line, Link, LinkText, LinkUrl}

import org.scalacheck.Gen

import scala.util.Random

object MarkdownGenerators:
  val linkGen: Gen[Link] =
    for
      linkText <- textGen()
      linkUrl <- urlGen
    yield Link(LinkText(linkText), LinkUrl(linkUrl))

  def textGen(minLength: Int = 3, maxLength: Int = 10): Gen[String] = for
    length <- Gen.choose(minLength, maxLength)
    text <- Gen.listOfN[Char](length, Gen.alphaNumChar).map(_.mkString)
  yield text

  def lineGen(linksGen: Gen[List[Link]] = Gen.listOf[Link](linkGen)): Gen[Line] = for
    numberOfFragments <- Gen.choose(0, 10)
    fragments <- Gen.listOfN[String](numberOfFragments, textGen())
    links <- linksGen.map(_.map { case Link(linkText, linkUrl) => s"[$linkText]($linkUrl)" })
    content = Random.shuffle(fragments ++ links).mkString(" ")
  yield Line(content)

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

  /** https://medium.com/@supermanue/building-useful-scalacheck-generators-71635d1edb9d */
  def urlGen: Gen[String] =
    for
      http <- httpTypeGen
      domain <- domainGen
      domainType <- domainTypeGen
      path <- pathGen
    yield http + "//" + domain + "." + domainType + "/" + path
