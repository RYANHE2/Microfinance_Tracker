package ui.screens

import scalafx.scene.Node
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Label, Button, ListView}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.Includes.*
import persistence.{BorrowerRepository, LoanRepository, RepaymentRepository}
import service.RiskEngine
import scala.annotation.nowarn

/** Feature 4: at-a-glance default-risk view across every loan. Pure
  * read/compute. no new persisted state, just RiskEngine applied over
  * whatever loans and repayments already exist.
  *
  * Borrower names are resolved here in the UI layer, not inside RiskEngine
  * RiskEngine stays a pure function of (loan, repayments) with no
  * repository access, so this screen is the one place that turns a
  * borrowerId into something a lender can actually read. Rows are also
  * sorted High-risk-first here, for the same reason: ordering loans by
  * urgency is a presentation decision, not a risk-scoring one.
  */
object RiskDashboardScreen:

  def apply(
      loanRepo: LoanRepository,
      repaymentRepo: RepaymentRepository,
      borrowerRepo: BorrowerRepository
  ): (Node, () => Unit) =
    val statusLabel = new Label(""):
      styleClass += "status-label"

    def setStatus(text: String, success: Boolean): Unit =
      statusLabel.text = text
      statusLabel.styleClass -= "status-success"
      statusLabel.styleClass -= "status-error"
      statusLabel.styleClass += (if success then "status-success" else "status-error")

    val listItems = ObservableBuffer.empty[String]
    val list = new ListView[String](listItems)

    def refresh(): Unit =
      val result = for
        loans      <- loanRepo.loadAll()
        repayments <- repaymentRepo.loadAll()
        borrowers  <- borrowerRepo.loadAll()
      yield
        val borrowerNames = borrowers.map(borrower => borrower.id -> borrower.name).toMap
        val assessed = loans.map { loan =>
          val loanRepayments = repayments.filter(_.loanId == loan.loanId)
          val risk = RiskEngine.assessRisk(loan, loanRepayments)
          val borrowerName = borrowerNames.getOrElse(loan.borrowerId, "Unknown borrower")
          val line = s"$borrowerName — ${RiskEngine.summarise(loan, loanRepayments)}"
          (risk, line)
        }
        // Highest risk first, so the loans that most need attention are
        // the first thing a lender sees, not buried under low-risk ones.
        assessed.sortBy { case (risk, _) => -risk.ordinal }.map { case (_, line) => line }

      result match
        case scala.util.Success(lines) =>
          listItems.setAll(lines*)
          setStatus(s"${lines.length} loan(s) assessed", success = true)
        case scala.util.Failure(ex) =>
          setStatus(s"Could not compute risk: ${ex.getMessage}", success = false)

    // Deliberately kept as the classic ScalaFX `handle{}` idiom (rather than
    // the `_ => {...}` form used elsewhere) as one clear example in the
    // codebase. ScalaFX 21 deprecated it in favour of plain function
    // literals, so the warning is suppressed on this val rather than left in.
    @nowarn("cat=deprecation")
    val refreshButton = new Button("Refresh"):
      styleClass += "primary-button"
      onAction = handle { refresh() }

    refresh()

    val title = new Label("Risk dashboard"):
      styleClass += "screen-title"

    val node = new VBox(14, title, refreshButton, statusLabel, list):
      padding = Insets(20)

    (node, () => refresh())
