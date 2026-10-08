package model

/** The staff member operating the app on behalf of the lending organisation. */
case class LoanOfficer(id: String, name: String, contact: String, employeeId: String)
    extends Person(id, name, contact):

  override def displayInfo(): String =
    s"${super.displayInfo()}, employee ID: $employeeId"
