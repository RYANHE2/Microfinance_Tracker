package persistence

import model.{Loan, MicroLoan, EmergencyLoan}

class LoanRepository extends JsonFileRepository[Loan]("loans.json"):

  protected def toJson(loan: Loan): ujson.Value =
    val base = ujson.Obj(
      "loanId" -> loan.loanId,
      "borrowerId" -> loan.borrowerId,
      "principal" -> loan.principal,
      "issueDate" -> loan.issueDate.toString
    )
    loan match
      case micro: MicroLoan =>
        base("type") = "micro"
        base("termMonths") = micro.termMonths
      case emergency: EmergencyLoan =>
        base("type") = "emergency"
        base("repaymentMonths") = emergency.repaymentMonths
    base

  protected def fromJson(json: ujson.Value): Loan =
    val loanId = json("loanId").str
    val borrowerId = json("borrowerId").str
    val principal = json("principal").num
    val issueDate = java.time.LocalDate.parse(json("issueDate").str)

    json("type").str match
      case "micro" =>
        MicroLoan(loanId, borrowerId, principal, issueDate, json("termMonths").num.toInt)
      case "emergency" =>
        EmergencyLoan(loanId, borrowerId, principal, issueDate, json("repaymentMonths").num.toInt)
      case other =>
        throw IllegalArgumentException(s"Unknown loan type in loans.json: $other")
