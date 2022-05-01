package es.eriktorr.markdown_transformation
package infrastructure

import model.{Link, LinksRepository, Reference}

import cats.effect.{IO, Ref}

final case class LinksRepositoryState(links: Map[Link, Reference])

object LinksRepositoryState:
  def empty: LinksRepositoryState = LinksRepositoryState(Map.empty)

final class InMemoryLinksRepository(stateRef: Ref[IO, LinksRepositoryState])
    extends LinksRepository:
  override def save(link: Link): IO[Reference] = stateRef.modify { currentState =>
    currentState.links.get(link) match
      case Some(reference) => (currentState, reference)
      case None =>
        val biggerReference = currentState.links.values.fold(Reference.zero)((x, y) =>
          if x.value > y.value then x else y,
        )
        (currentState, Reference(biggerReference.value + 1))
  }
