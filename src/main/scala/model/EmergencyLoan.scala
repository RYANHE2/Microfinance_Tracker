package model

import java.time.LocalDate

/** Short-term, low/no-interest loan for urgent needs (medical bills, disaster
  * relief). Capped principal, interest waived if repaid within 3 months.
  *
  * Constructor params are named `amount`/`date` rather than `principal`/
  * `issueDate` for the same reason as in MicroLoan — see its doc comment.
  * Use the inherited `principal`/`issueDate` names inside this class,
  * not `amount`/`date`.
  */
case class EmergencyLoan(
    id: String,
    borrowerRefId: String,
    amount: Double,
    date: LocalDate,
    repaymentMonths: Int
) extends Loan(id, borrowerRefId, amount, date):

  protected val baseRatePercent: Double = 1.0

  override def calculateInterest(): Double =
    if repaymentMonths <= 3 then 0.0
    else principal * (baseRatePercent / 100.0) * (repaymentMonths / 12.0)

  override def loanType: String = "Emergency loan"

  override def validate(): Either[String, Unit] =
    if repaymentMonths <= 0 then Left("Repayment window must be at least 1 month")
    else if principal > 2000 then Left("Emergency loans are capped at 2000")
    else super.validate()
