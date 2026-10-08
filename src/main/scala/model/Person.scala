package model

/** Common base for anyone the system tracks by identity: a borrower or a
  * loan officer. Kept abstract because "a person" on its own is never
  * meaningful in this domain — only its concrete roles are.
  */
abstract class Person(val personId: String, val fullName: String, val contactNumber: String):

  /** Subclasses add role-specific detail on top of this. */
  def displayInfo(): String =
    s"$fullName (ID: $personId, contact: $contactNumber)"
