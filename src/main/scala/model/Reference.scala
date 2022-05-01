package es.eriktorr.markdown_transformation
package model

import monix.newtypes.*

type Reference = Reference.Type

object Reference extends NewtypeWrapped[Int]:
  val zero: Reference = Reference(0)
