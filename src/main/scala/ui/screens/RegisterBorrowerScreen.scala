package ui.screens

import scalafx.scene.Node
import scalafx.scene.layout.{VBox, GridPane}
import scalafx.scene.control.{Label, TextField, Button, ListView}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import model.Borrower
import persistence.BorrowerRepository

/** Feature 1: register a new borrower and see the running list. */
object RegisterBorrowerScreen:

  private def formLabel(text: String): Label =
    new Label(text):
      styleClass += "form-label"

  def apply(repo: BorrowerRepository): (Node, () => Unit) =
    val nameField    = new TextField()
    nameField.promptText = "Full name"
    val contactField = new TextField()
    contactField.promptText = "Contact number (digits only, 7-15)"
    val incomeField  = new TextField()
    incomeField.promptText = "Monthly household income"
    val dependents   = new TextField()
    dependents.promptText = "Number of dependents"

    val statusLabel = new Label(""):
      styleClass += "status-label"

    def setStatus(text: String, success: Boolean): Unit =
      statusLabel.text = text
      statusLabel.styleClass -= "status-success"
      statusLabel.styleClass -= "status-error"
      statusLabel.styleClass += (if success then "status-success" else "status-error")

    val listItems = ObservableBuffer.empty[String]
    val list = new ListView[String](listItems)

    def refreshList(): Unit =
      repo.loadAll() match
        case scala.util.Success(borrowers) =>
          listItems.setAll(borrowers.map(_.displayInfo())*)
        case scala.util.Failure(ex) =>
          setStatus(s"Could not load borrowers: ${ex.getMessage}", success = false)

    def clearForm(): Unit =
      nameField.text = ""
      contactField.text = ""
      incomeField.text = ""
      dependents.text = ""

    val saveButton = new Button("Register borrower"):
      styleClass += "primary-button"
      defaultButton = true
      onAction = _ => {
        val incomeParsed = incomeField.text.value.toDoubleOption
        val depsParsed = dependents.text.value.toIntOption

        val result = for
          income <- incomeParsed.toRight("Income must be a number")
          deps   <- depsParsed.toRight("Dependents must be a whole number")
          borrower = Borrower(
            id = java.util.UUID.randomUUID().toString.take(8),
            name = nameField.text.value,
            contact = contactField.text.value,
            householdIncome = income,
            numDependents = deps
          )
          _ <- borrower.validate()
        yield borrower

        result match
          case Right(borrower) =>
            repo.save(borrower) match
              case scala.util.Success(_) =>
                setStatus(s"Registered ${borrower.name}", success = true)
                clearForm()
                refreshList()
              case scala.util.Failure(ex) =>
                setStatus(s"Could not save: ${ex.getMessage}", success = false)
          case Left(reason) =>
            setStatus(reason, success = false)
      }

    val form = new GridPane():
      styleClass += "card"
      hgap = 10
      vgap = 10
      add(formLabel("Name:"), 0, 0);        add(nameField, 1, 0)
      add(formLabel("Contact:"), 0, 1);     add(contactField, 1, 1)
      add(formLabel("Income:"), 0, 2);      add(incomeField, 1, 2)
      add(formLabel("Dependents:"), 0, 3);  add(dependents, 1, 3)
      add(saveButton, 1, 4)

    refreshList()

    val title = new Label("Register a borrower"):
      styleClass += "screen-title"

    val node = new VBox(14, title, form, statusLabel, list):
      padding = Insets(20)

    (node, () => refreshList())
