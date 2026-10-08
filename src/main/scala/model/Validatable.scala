package model

/** Mixed into any model whose fields need checking before it's accepted
  * into the system. Keeping this as a trait, rather than duplicating
  * checks everywhere, means every screen's "save" handler calls the same
  * `validate()` contract instead of each model re-implementing its own
  * ad-hoc checks.
  */
trait Validatable:
  /** Right(()) if valid, Left(message) with a user-facing reason otherwise. */
  def validate(): Either[String, Unit]
