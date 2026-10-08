package service

// ai-assisted: #6
// why: wanted a starting point for turning loan + repayment history into
// a risk score; refined the thresholds myself afterwards

import model.{Loan, Repayment, RiskLevel}
import java.time.LocalDate

/** Pure functions only — no repository access, no side effects. Given a
  * loan and its repayment history, decide how worried the lender should be.
  * Kept as a standalone object (not a method on Loan) because risk scoring
  * is a *policy* decision, separate from what a loan fundamentally is.
  */
object RiskEngine:

  private val overdueDaysGrace = 30

  def assessRisk(loan: Loan, repayments: List[Repayment], today: LocalDate = LocalDate.now()): RiskLevel =
    val totalRepaid = repayments.map(_.amount).sum
    val outstanding = loan.totalRepayable - totalRepaid
    val daysSinceIssue = java.time.temporal.ChronoUnit.DAYS.between(loan.issueDate, today)

    if outstanding <= 0 then RiskLevel.Low
    else if daysSinceIssue > overdueDaysGrace && totalRepaid == 0 then RiskLevel.High
    else if daysSinceIssue > overdueDaysGrace then RiskLevel.Medium
    else RiskLevel.Low

  /** Summary line for the dashboard table. */
  def summarise(loan: Loan, repayments: List[Repayment]): String =
    val risk = assessRisk(loan, repayments)
    val outstanding = loan.totalRepayable - repayments.map(_.amount).sum
    s"${loan.loanType} ${loan.loanId}: outstanding ${"%.2f".format(outstanding)}, risk: $risk"
