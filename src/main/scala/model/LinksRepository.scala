package es.eriktorr.markdown_transformation
package model

import cats.effect.IO

trait LinksRepository:
  def save(link: Link): IO[Reference]
