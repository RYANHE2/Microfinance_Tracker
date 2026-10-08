package ui.screens

import scalafx.scene.Node
import scalafx.scene.layout.{VBox, GridPane}
import scalafx.scene.control.{Label, TextField, Button, ComboBox, ListView}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import model.{Borrower, MicroLoan, EmergencyLoan, Loan}
import persistence.{BorrowerRepository, LoanRepository}
import java.time.LocalDate

/** Feature 2: pick a borrower and disburse either loan type. This is where
  * the two Loan subclasses actually diverge in the UI — Emergency loans
  * take a repayment-window field, Micro loans take a term-in-months field.
  */
object DisburseLoanScreen:

  private val loanTypes = ObservableBuffer("Micro loan", "Emergency loan")

  private def formLabel(text: String): Label =
    new Label(text):
      styleClass += "form-label"

  def apply(borrowerRepo: BorrowerRepository, loanRepo: LoanRepository): (Node, () => Unit) =
    val statusLabel = new Label(""):
      styleClass += "status-label"

    def setStatus(text: String, success: Boolean): Unit =
      statusLabel.text = text
      statusLabel.styleClass -= "status-success"
      statusLabel.styleClass -= "status-error"
      statusLabel.styleClass += (if success then "status-success" else "status-error")

    val borrowerBuffer = ObservableBuffer.empty[Borrower]
    val borrowerCombo = new ComboBox[String]()
    val typeCombo = new ComboBox[String](loanTypes):
      value = loanTypes.head

    val amountField = new TextField()
    amountField.promptText = "Loan amount"
    val termField    = new TextField()
    termField.promptText = "Term / repayment window (months)"

    val listItems = ObservableBuffer.empty[String]
    val list = new ListView[String](listItems)

    def refreshBorrowers(): Unit =
      borrowerRepo.loadAll() match
        case scala.util.Success(borrowers) =>
          borrowerBuffer.setAll(borrowers*)
          borrowerCombo.items = ObservableBuffer(borrowers.map(borrower => s"${borrower.name} (${borrower.id})")*)
        case scala.util.Failure(ex) =>
          setStatus(s"Could not load borrowers: ${ex.getMessage}", success = false)

    def refreshLoans(): Unit =
      loanRepo.loadAll() match
        case scala.util.Success(loans) =>
          val borrowerNames = borrowerBuffer.toList.map(borrower => borrower.id -> borrower.name).toMap
          listItems.setAll(loans.map { loan =>
            val borrowerName = borrowerNames.getOrElse(loan.borrowerId, "Unknown borrower")
            f"${loan.loanType} ${loan.loanId}: ${loan.principal}%.2f to $borrowerName"
          }*)
        case scala.util.Failure(ex) =>
          setStatus(s"Could not load loans: ${ex.getMessage}", success = false)

    val disburseButton = new Button("Disburse loan"):
      styleClass += "primary-button"
      defaultButton = true
      onAction = _ => {
        val selectedIndex = borrowerCombo.selectionModel.value.getSelectedIndex
        val amountParsed = amountField.text.value.toDoubleOption
        val termParsed = termField.text.value.toIntOption

        val result: Either[String, Loan] =
          for
            _        <- Either.cond(selectedIndex >= 0, (), "Select a borrower first")
            borrower  = borrowerBuffer(selectedIndex)
            amount   <- amountParsed.toRight("Amount must be a number")
            term     <- termParsed.toRight("Term must be a whole number")
            loan = typeCombo.value.value match
              case "Emergency loan" =>
                EmergencyLoan(java.util.UUID.randomUUID().toString.take(8), borrower.id, amount, LocalDate.now(), term)
              case _ =>
                MicroLoan(java.util.UUID.randomUUID().toString.take(8), borrower.id, amount, LocalDate.now(), term)
            _ <- loan.validate()
          yield loan

        result match
          case Right(loan) =>
            loanRepo.save(loan) match
              case scala.util.Success(_) =>
                setStatus(s"Disbursed ${loan.loanType} ${loan.loanId}", success = true)
                amountField.text = ""
                termField.text = ""
                refreshLoans()
              case scala.util.Failure(ex) =>
                setStatus(s"Could not save loan: ${ex.getMessage}", success = false)
          case Left(reason) =>
            setStatus(reason, success = false)
      }

    val form = new GridPane():
      styleClass += "card"
      hgap = 10
      vgap = 10
      add(formLabel("Borrower:"), 0, 0);  add(borrowerCombo, 1, 0)
      add(formLabel("Loan type:"), 0, 1); add(typeCombo, 1, 1)
      add(formLabel("Amount:"), 0, 2);    add(amountField, 1, 2)
      add(formLabel("Term (months):"), 0, 3); add(termField, 1, 3)
      add(disburseButton, 1, 4)

    refreshBorrowers()
    refreshLoans()

    val title = new Label("Disburse a loan"):
      styleClass += "screen-title"

    val node = new VBox(14, title, form, statusLabel, list):
      padding = Insets(20)

    (node, () => { refreshBorrowers(); refreshLoans() })
