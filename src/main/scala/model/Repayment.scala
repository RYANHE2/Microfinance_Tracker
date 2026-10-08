package model

import java.time.LocalDate

/** One repayment event against a loan. Repayment history for a loan is just
  * `List[Repayment]` filtered by loanId — no mutable ledger anywhere.
  */
case class Repayment(loanId: String, amount: Double, date: LocalDate, method: String)
    extends Validatable:

  override def validate(): Either[String, Unit] =
    if amount <= 0 then Left("Repayment amount must be greater than zero")
    else Right(())
