package forms

import play.api.data.Form
import play.api.data.Forms._
import models.Account

object SignInForm {

  case class LoginUser( email : String, password : String)

  val form = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Account.authenticate)(_.map(u => (u.email, ""))).verifying("Invalid email or password", result => result.isDefined)
  }
}
