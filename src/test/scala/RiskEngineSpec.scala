package service

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.{MicroLoan, Repayment, RiskLevel}
import java.time.LocalDate

/** Covers RiskEngine only, since it is the one pure, dependency-free piece
  * of logic in the project, no repositories, no UI, no filesystem. That
  * makes it the cheapest thing to test and the most worth testing: a
  * wrong risk score is a silent bug nobody would notice just by clicking
  * through the app.
  */
class RiskEngineSpec extends AnyFlatSpec with Matchers:

  private val today = LocalDate.of(2026, 8, 3)

  private def loan(issueDate: LocalDate, principal: Double = 1000.0): MicroLoan =
    MicroLoan(
      id = "test-loan",
      borrowerRefId = "test-borrower",
      amount = principal,
      date = issueDate,
      termMonths = 12
    )

  "assessRisk" should "be Low when the loan is fully repaid, no matter how overdue it is" in {
    val oldLoan = loan(issueDate = today.minusDays(200))
    val fullRepayment = Repayment(oldLoan.loanId, oldLoan.totalRepayable, today, "cash")

    RiskEngine.assessRisk(oldLoan, List(fullRepayment), today) shouldBe RiskLevel.Low
  }

  it should "be Low within the grace period even with zero repayments" in {
    val recentLoan = loan(issueDate = today.minusDays(10))

    RiskEngine.assessRisk(recentLoan, Nil, today) shouldBe RiskLevel.Low
  }

  it should "be High when overdue past the grace period with zero repayments" in {
    val overdueLoan = loan(issueDate = today.minusDays(45))

    RiskEngine.assessRisk(overdueLoan, Nil, today) shouldBe RiskLevel.High
  }

  it should "be Medium when overdue past the grace period but something has been repaid" in {
    val overdueLoan = loan(issueDate = today.minusDays(45))
    val partialRepayment = Repayment(overdueLoan.loanId, 50.0, today, "cash")

    RiskEngine.assessRisk(overdueLoan, List(partialRepayment), today) shouldBe RiskLevel.Medium
  }

  "summarise" should "include the loan type and id in its output" in {
    val recentLoan = loan(issueDate = today.minusDays(5))

    val line = RiskEngine.summarise(recentLoan, Nil)

    line should include(recentLoan.loanType)
    line should include(recentLoan.loanId)
  }
