package model

// ai-assisted: #4
// why: needed an idiomatic Scala 3 pattern for an abstract class,
// specifically one with a protected abstract val plus an abstract
// method, for the interest calc

import java.time.LocalDate

/** A disbursed loan. Concrete loan types differ only in how interest is
  * calculated, so that's the one abstract method — everything else about
  * "being a loan" (id, borrower, principal, repayable total) is shared here.
  */
abstract class Loan(
    val loanId: String,
    val borrowerId: String,
    val principal: Double,
    val issueDate: LocalDate
) extends Validatable:

  // protected, not public: subclasses need the base rate to compute interest,
  // but UI/service code must always go through calculateInterest() /
  // totalRepayable so the interest formula can change without breaking callers.
  protected val baseRatePercent: Double

  /** Each loan type defines its own interest rule. */
  def calculateInterest(): Double

  def totalRepayable: Double = principal + calculateInterest()

  def loanType: String

  override def validate(): Either[String, Unit] =
    if principal <= 0 then Left("Loan principal must be greater than zero")
    else if principal > 1_000_000 then Left("Loan principal is unrealistically large")
    else Right(())
