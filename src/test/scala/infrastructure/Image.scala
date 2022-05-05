package es.eriktorr.markdown_transformation
package infrastructure

import monix.newtypes.*

type ImageCaption = ImageCaption.Type

object ImageCaption extends NewtypeWrapped[String]

type ImageUrl = ImageUrl.Type

object ImageUrl extends NewtypeWrapped[String]

final case class Image(caption: ImageCaption, url: ImageUrl)
