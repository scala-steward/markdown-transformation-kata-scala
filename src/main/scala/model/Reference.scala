package es.eriktorr.markdown_transformation
package model

import monix.newtypes.*

type Reference = Reference.Type

object Reference extends NewtypeWrapped[Int]:
  val zero: Reference = Reference(0)

  implicit final class ReferenceOps(val self: Reference):
    def increment: Reference = Reference(self.value + 1)
