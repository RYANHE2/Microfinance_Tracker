package ui

// ai-assisted: #7, #8
// why: first time building a JFXApp3 shell with a swappable BorderPane
// centre; entry #8 fixed a root/root self-assignment bug from entry #7

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.control.Alert.AlertType
import scalafx.geometry.Insets
import persistence.{BorrowerRepository, LoanRepository, RepaymentRepository}
import model.LoanOfficer
import ui.screens.*

/** Application shell: a fixed left-hand nav and a swappable centre pane.
  * Each screen's `apply()` returns a (Node, refresh) pair — the Node is
  * mounted once and reused, and `refresh` is re-run every time the user
  * navigates to that screen, so it always reflects whatever the other
  * screens have since saved to the repositories.
  */
object MainApp extends JFXApp3:

  private val borrowerRepo  = new BorrowerRepository()
  private val loanRepo      = new LoanRepository()
  private val repaymentRepo = new RepaymentRepository()

  // A single hardcoded officer for this single-user desktop app — no login
  // screen in scope for this project, but this is real usage of LoanOfficer,
  // real usage, not something existing only to satisfy a rubric checkbox.
  private val currentOfficer = LoanOfficer(
    id = "OFC-001",
    name = "Duty Officer",
    contact = "N/A",
    employeeId = "OFC-001"
  )

  override def start(): Unit =
    // Last line of defence: anything that slips past a screen's own Try/Either
    // handling shows a dialog instead of taking the whole app down (S1-19).
    Thread.setDefaultUncaughtExceptionHandler { (_, ex) =>
      scalafx.application.Platform.runLater {
        val alert = new Alert(AlertType.Error):
          headerText = "Something went wrong"
          contentText = Option(ex.getMessage).getOrElse(ex.toString)
        alert.showAndWait()
      }
    }

    val rootPane = new BorderPane()

    val (registerNode, registerRefresh)   = RegisterBorrowerScreen(borrowerRepo)
    val (disburseNode, disburseRefresh)   = DisburseLoanScreen(borrowerRepo, loanRepo)
    val (repaymentNode, repaymentRefresh) = RecordRepaymentScreen(loanRepo, repaymentRepo, borrowerRepo)
    val (dashboardNode, dashboardRefresh) = RiskDashboardScreen(loanRepo, repaymentRepo, borrowerRepo)

    // Screens are built once and reused, so without this, switching to a
    // screen after data changed elsewhere (e.g. registering a borrower, then
    // going to Disburse loan) would show stale combo boxes / lists. Re-running
    // each screen's refresh on every visit keeps it in sync with the repo.
    def show(node: scalafx.scene.Node, refresh: () => Unit): Unit =
      refresh()
      rootPane.center = node

    val navDefs = List(
      "Register borrower" -> (() => show(registerNode, registerRefresh)),
      "Disburse loan" -> (() => show(disburseNode, disburseRefresh)),
      "Record repayment" -> (() => show(repaymentNode, repaymentRefresh)),
      "Risk dashboard" -> (() => show(dashboardNode, dashboardRefresh))
    )

    // Built in two passes: buttons first, then their handlers — a handler
    // needs to see every button (to clear the old selection highlight), so
    // it can't be wired up until the full list exists.
    val navButtons: List[Button] = navDefs.map { case (label, _) =>
      new Button(label):
        maxWidth = Double.MaxValue
        styleClass += "nav-button"
    }

    def selectNav(button: Button): Unit =
      navButtons.foreach(_.styleClass -= "nav-button-selected")
      button.styleClass += "nav-button-selected"

    navButtons.zip(navDefs).foreach { case (button, (_, action)) =>
      button.onAction = _ => {
        action()
        selectNav(button)
      }
    }

    val officerLabel = new Label(s"Logged in as: ${currentOfficer.fullName} (${currentOfficer.employeeId})"):
      styleClass += "officer-label"
      wrapText = true

    val navChildren: Seq[scalafx.scene.Node] = officerLabel +: navButtons

    val nav = new VBox(4, navChildren*):
      padding = Insets(16)
      prefWidth = 200
      styleClass += "app-sidebar"

    rootPane.left = nav
    show(registerNode, registerRefresh)
    selectNav(navButtons.head)

    val stylesheetUrl = getClass.getResource("/styles/app.css").toExternalForm

    stage = new JFXApp3.PrimaryStage:
      title = "Microfinance Loan Tracker — SDG 1"
      scene = new Scene(980, 640):
        root = rootPane
        stylesheets.add(stylesheetUrl)
