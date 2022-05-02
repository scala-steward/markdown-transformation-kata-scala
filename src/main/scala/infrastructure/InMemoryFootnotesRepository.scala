package es.eriktorr.markdown_transformation
package infrastructure

import model.{Footnote, FootnotesRepository, Link, Reference}

import cats.effect.{IO, Ref}

final case class FootnotesRepositoryState(footnotes: List[Footnote]):
  def set(newFootnotes: List[Footnote]): FootnotesRepositoryState = copy(footnotes = newFootnotes)

object FootnotesRepositoryState:
  def empty: FootnotesRepositoryState = FootnotesRepositoryState(List.empty)

final class InMemoryFootnotesRepository(stateRef: Ref[IO, FootnotesRepositoryState])
    extends FootnotesRepository:
  override def save(link: Link): IO[Reference] = stateRef.modify { currentState =>
    currentState.footnotes.find(_.link == link) match
      case Some(Footnote(reference, _)) => (currentState, reference)
      case None =>
        val lastReference = currentState.footnotes
          .map(_.reference)
          .fold(Reference.zero)((x, y) => if x.value > y.value then x else y)
        val reference = Reference(lastReference.value + 1)
        (
          currentState.copy(footnotes = Footnote(reference, link) :: currentState.footnotes),
          reference,
        )
  }

  override def footnotes: IO[List[Footnote]] = stateRef.get.map(_.footnotes)
