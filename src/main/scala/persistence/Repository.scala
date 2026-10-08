package persistence

import scala.util.Try

/** Generic persistence contract. One trait, implemented once per model type,
  * instead of hand-rolling save/load/find for Borrower, Loan, and Repayment
  * separately — that repetition is exactly what S1-13 (DRY) is checking for.
  *
  * @tparam T the domain type being persisted
  */
trait Repository[T]:
  def save(item: T): Try[Unit]
  def saveAll(items: List[T]): Try[Unit]
  def loadAll(): Try[List[T]]
