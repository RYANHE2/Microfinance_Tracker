package model

/** A household enrolled to receive microloans.
  *
  * @param householdIncome monthly income in local currency, used by RiskEngine
  * @param numDependents    people financially dependent on this borrower
  */
case class Borrower(
    id: String,
    name: String,
    contact: String,
    householdIncome: Double,
    numDependents: Int
) extends Person(id, name, contact)
    with Validatable:

  override def displayInfo(): String =
    s"${super.displayInfo()}, income: $householdIncome, dependents: $numDependents"


  override def validate(): Either[String, Unit] =
    val digitsOnly = contact.trim.stripPrefix("+")
    if name.trim.isEmpty then Left("Borrower name cannot be empty")
    else if contact.trim.isEmpty then Left("Contact number cannot be empty")
    else if !digitsOnly.forall(_.isDigit) then Left("Contact number must contain only digits (an optional leading + is allowed)")
    else if digitsOnly.length < 7 then Left("Contact number is too short (minimum 7 digits)")
    else if digitsOnly.length > 15 then Left("Contact number is too long (maximum 15 digits)")
    else if householdIncome < 0 then Left("Household income cannot be negative")
    else if numDependents < 0 then Left("Number of dependents cannot be negative")
    else if numDependents > 20 then Left("Number of dependents looks like a typo (over 20)")
    else Right(())
