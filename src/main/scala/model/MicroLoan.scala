package model

import java.time.LocalDate

/** Standard microfinance loan: flat interest on the full term.
  *
  * Constructor params are named `amount`/`date` rather than `principal`/
  * `issueDate` deliberately. Loan already defines those as concrete vals,
  * and a case class, once declared, cannot redeclare an inherited val
  * under the same name without an explicit `override val`. Once inside
  * the class, use the inherited `principal`/`issueDate` names, not
  * `amount`/`date`, so there's one consistent name for "the loan amount"
  * throughout the codebase.
  */
case class MicroLoan(
    id: String,
    borrowerRefId: String,
    amount: Double,
    date: LocalDate,
    termMonths: Int
) extends Loan(id, borrowerRefId, amount, date):

  protected val baseRatePercent: Double = 5.0

  override def calculateInterest(): Double =
    principal * (baseRatePercent / 100.0) * (termMonths / 12.0)

  override def loanType: String = "Micro loan"

  override def validate(): Either[String, Unit] =
    if termMonths <= 0 then Left("Term must be at least 1 month")
    else if termMonths > 60 then Left("Micro loan term cannot exceed 60 months")
    else super.validate()
