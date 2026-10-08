package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Covers Borrower.validate(), especially the contact number rule added
  * after the fact, so there's a record of exactly what "valid" is meant
  * to mean here if that rule ever needs to change.
  */
class BorrowerSpec extends AnyFlatSpec with Matchers:

  private def borrower(contact: String): Borrower =
    Borrower(id = "b1", name = "Jane Doe", contact = contact, householdIncome = 1000.0, numDependents = 1)

  "validate" should "accept a plain digits-only number in range" in {
    borrower("0123456").validate() shouldBe Right(())
  }

  it should "accept a number with a leading +" in {
    borrower("+60123456789").validate() shouldBe Right(())
  }

  it should "reject a number that is too short" in {
    borrower("12345").validate() shouldBe a[Left[?, ?]]
  }

  it should "reject a number that is too long" in {
    borrower("1234567890123456").validate() shouldBe a[Left[?, ?]]
  }

  it should "reject a number containing letters" in {
    borrower("012-345-ABCD").validate() shouldBe a[Left[?, ?]]
  }

  it should "reject an empty contact number" in {
    borrower("").validate() shouldBe a[Left[?, ?]]
  }
