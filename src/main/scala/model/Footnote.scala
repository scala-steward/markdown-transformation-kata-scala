package es.eriktorr.markdown_transformation
package model

final case class Footnote(reference: Reference, link: Link)

object Footnote:
  implicit val footnoteOrdering: Ordering[Footnote] = Ordering.by(_.reference.value)
