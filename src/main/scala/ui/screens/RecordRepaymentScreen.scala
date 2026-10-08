package ui.screens

// ai-assisted: #9
// why: needed help chaining several Either-based validation steps
// (selection, parsing, model validation, overpayment check) cleanly

import scalafx.scene.Node
import scalafx.scene.layout.{VBox, GridPane}
import scalafx.scene.control.{Label, TextField, Button, ComboBox, ListView}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import model.{Loan, Repayment}
import persistence.{BorrowerRepository, LoanRepository, RepaymentRepository}
import java.time.LocalDate

/** Feature 3: record a repayment against an existing loan. */
object RecordRepaymentScreen:

  private def formLabel(text: String): Label =
    new Label(text):
      styleClass += "form-label"

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

    val loanBuffer = ObservableBuffer.empty[Loan]
    val loanCombo = new ComboBox[String]()
    val amountField = new TextField()
    amountField.promptText = "Repayment amount"
    val methodField = new TextField()
    methodField.promptText = "Method (cash, transfer, ...)"

    val listItems = ObservableBuffer.empty[String]
    val list = new ListView[String](listItems)

    def refreshLoans(): Unit =
      loanRepo.loadAll() match
        case scala.util.Success(loans) =>
          loanBuffer.setAll(loans*)
          loanCombo.items = ObservableBuffer(loans.map(loan => s"${loan.loanType} ${loan.loanId}")*)
        case scala.util.Failure(ex) =>
          setStatus(s"Could not load loans: ${ex.getMessage}", success = false)

    def refreshRepayments(): Unit =
      val result = for
        repayments <- repaymentRepo.loadAll()
        borrowers  <- borrowerRepo.loadAll()
      yield
        val borrowerNames = borrowers.map(borrower => borrower.id -> borrower.name).toMap
        val loanBorrowerIds = loanBuffer.toList.map(loan => loan.loanId -> loan.borrowerId).toMap
        repayments.map { repayment =>
          val borrowerName = loanBorrowerIds.get(repayment.loanId)
            .flatMap(borrowerNames.get)
            .getOrElse("Unknown borrower")
          f"$borrowerName — ${repayment.loanId}: ${repayment.amount}%.2f on ${repayment.date} (${repayment.method})"
        }

      result match
        case scala.util.Success(lines) =>
          listItems.setAll(lines*)
        case scala.util.Failure(ex) =>
          setStatus(s"Could not load repayments: ${ex.getMessage}", success = false)

    val recordButton = new Button("Record repayment"):
      styleClass += "primary-button"
      defaultButton = true
      onAction = _ => {
        val selectedIndex = loanCombo.selectionModel.value.getSelectedIndex
        val amountParsed = amountField.text.value.toDoubleOption

        val result: Either[String, Repayment] =
          for
            _        <- Either.cond(selectedIndex >= 0, (), "Select a loan first")
            loan      = loanBuffer(selectedIndex)
            amount   <- amountParsed.toRight("Amount must be a number")
            method    = if methodField.text.value.trim.isEmpty then "unspecified" else methodField.text.value
            repayment = Repayment(loan.loanId, amount, LocalDate.now(), method)
            _        <- repayment.validate()
            existing <- repaymentRepo.findByLoan(loan.loanId).toEither.left.map(_.getMessage)
            alreadyPaid = existing.map(_.amount).sum
            outstanding = loan.totalRepayable - alreadyPaid
            _        <- Either.cond(
                          amount <= outstanding + 0.01,
                          (),
                          f"Amount exceeds outstanding balance ($outstanding%.2f remaining)"
                        )
          yield repayment

        result match
          case Right(repayment) =>
            repaymentRepo.save(repayment) match
              case scala.util.Success(_) =>
                setStatus(s"Recorded repayment for ${repayment.loanId}", success = true)
                amountField.text = ""
                methodField.text = ""
                refreshRepayments()
              case scala.util.Failure(ex) =>
                setStatus(s"Could not save repayment: ${ex.getMessage}", success = false)
          case Left(reason) =>
            setStatus(reason, success = false)
      }

    val form = new GridPane():
      styleClass += "card"
      hgap = 10
      vgap = 10
      add(formLabel("Loan:"), 0, 0);    add(loanCombo, 1, 0)
      add(formLabel("Amount:"), 0, 1);  add(amountField, 1, 1)
      add(formLabel("Method:"), 0, 2);  add(methodField, 1, 2)
      add(recordButton, 1, 3)

    refreshLoans()
    refreshRepayments()

    val title = new Label("Record a repayment"):
      styleClass += "screen-title"

    val node = new VBox(14, title, form, statusLabel, list):
      padding = Insets(20)

    (node, () => { refreshLoans(); refreshRepayments() })
