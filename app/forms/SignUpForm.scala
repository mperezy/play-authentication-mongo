package forms

import models.Account
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import reactivemongo.bson.{BSONDateTime, BSONObjectID}

object SignUpForm {
  val form = Form(
    mapping(
      " _id" -> ignored(Option.empty[BSONObjectID]),
      "role" -> optional(nonEmptyText),
      "useremail" -> email,
      "firstname" -> optional(nonEmptyText),
      "lastname" -> optional(nonEmptyText),
      "password" -> nonEmptyText,
      "create_date" -> ignored(Option.empty[BSONDateTime]),
      "update_date" -> ignored(Option.empty[BSONDateTime])
    )(Account.apply)(Account.unapply)
      .verifying(
        "Too few characters. It should have more than 3 characters.",
        user => !(user.password.length < 4)
      )
  )
}
