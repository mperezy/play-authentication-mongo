package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.Account
import reactivemongo.bson.{BSONDateTime, BSONObjectID}

object SignUpForm {

  var fewCharactersErrorMessage = ""

  val form = Form(
    mapping(
      " _id" -> ignored(Option.empty[BSONObjectID]),
      "role" -> optional(nonEmptyText),
      "email" -> nonEmptyText,
      "firstName" -> optional(nonEmptyText),
      "lastName" -> optional(nonEmptyText),
      "password" -> nonEmptyText,
      "create_date" -> ignored(Option.empty[BSONDateTime]),
      "update_date" -> ignored(Option.empty[BSONDateTime])
    )(Account.apply)(Account.unapply)
      .verifying(fewCharactersErrorMessage, user => !isShortPassword(user, 4))
  )

  def isShortPassword(user: Account, min: Int): Boolean = {
    if (user.password.length < min) {
      fewCharactersErrorMessage = s"Too few characters. It should have more than 3 characters.#${user.firstName.getOrElse("")}&${user.lastName.getOrElse("")}&${user.email}"
      true
    } else false
  }
}
