package persistence

import model.Borrower

class BorrowerRepository extends JsonFileRepository[Borrower]("borrowers.json"):

  protected def toJson(borrower: Borrower): ujson.Value =
    ujson.Obj(
      "id" -> borrower.id,
      "name" -> borrower.name,
      "contact" -> borrower.contact,
      "householdIncome" -> borrower.householdIncome,
      "numDependents" -> borrower.numDependents
    )

  protected def fromJson(json: ujson.Value): Borrower =
    Borrower(
      id = json("id").str,
      name = json("name").str,
      contact = json("contact").str,
      householdIncome = json("householdIncome").num,
      numDependents = json("numDependents").num.toInt
    )
