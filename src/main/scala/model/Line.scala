package es.eriktorr.markdown_transformation
package model

import monix.newtypes.*

type Line = Line.Type

object Line extends NewtypeWrapped[String]:
  val empty: Line = Line("")
