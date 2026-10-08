package persistence

import model.Repayment
import java.time.LocalDate

class RepaymentRepository extends JsonFileRepository[Repayment]("repayments.json"):

  protected def toJson(repayment: Repayment): ujson.Value =
    ujson.Obj(
      "loanId" -> repayment.loanId,
      "amount" -> repayment.amount,
      "date" -> repayment.date.toString,
      "method" -> repayment.method
    )

  protected def fromJson(json: ujson.Value): Repayment =
    Repayment(
      loanId = json("loanId").str,
      amount = json("amount").num,
      date = LocalDate.parse(json("date").str),
      method = json("method").str
    )

  /** Convenience used by the risk dashboard and the repayment screen. */
  def findByLoan(loanId: String): scala.util.Try[List[Repayment]] =
    loadAll().map(_.filter(_.loanId == loanId))
