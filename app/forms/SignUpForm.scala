package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.Account
import reactivemongo.bson.BSONObjectID

object SignUpForm {
  val form = Form(
    mapping(
      " _id" -> ignored(Option.empty[BSONObjectID]),
      "role" -> optional(nonEmptyText),
      "email" -> nonEmptyText,
      "firstName" -> optional(nonEmptyText),
      "lastName" -> optional(nonEmptyText),
      "password" -> nonEmptyText,
      "avatar_path" -> optional(nonEmptyText),
      "update_date" -> optional(nonEmptyText)
    )(Account.apply)(Account.unapply)
    .verifying("Too few characters. It should have more than 3 characters.", user => !isShortPassword(user.password, 4))
  )

  def isShortPassword(string: String, min: Int): Boolean = {
    if (string.length < min) true else false
  }
}
