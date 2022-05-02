package es.eriktorr.markdown_transformation
package model

import cats.effect.IO

trait FootnotesRepository:
  def save(link: Link): IO[Reference]
  def footnotes: IO[List[Footnote]]
