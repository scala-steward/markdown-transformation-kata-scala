package es.eriktorr.markdown_transformation
package model

import cats.implicits.*
import cats.{derived, Eq, Show}
import monix.newtypes.*

type LinkText = LinkText.Type

object LinkText extends NewtypeWrapped[String]:
  implicit val eq: Eq[LinkText] = derive(using Eq.catsKernelInstancesForString)
  implicit val show: Show[LinkText] = derive(using Show.catsShowForString)

type LinkUrl = LinkUrl.Type

object LinkUrl extends NewtypeWrapped[String]:
  implicit val eq: Eq[LinkUrl] = derive(using Eq.catsKernelInstancesForString)
  implicit val show: Show[LinkUrl] = derive(using Show.catsShowForString)

final case class Link(text: LinkText, url: LinkUrl)

object Link:
  implicit val eq: Eq[Link] = derived.semiauto.eq
  implicit val show: Show[Link] = derived.semiauto.show
