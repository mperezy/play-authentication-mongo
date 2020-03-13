package forms

import models.Account
import play.api.data.Form
import play.api.data.Forms._

object SignInForm {
  case class LoginAccount(email: String, password: String)

  val form = Form {
    mapping("email" -> email, "password" -> nonEmptyText)(LoginAccount.apply)(
      LoginAccount.unapply
    ).verifying(
        "That email is not registered.",
        result => Account.findByEmail(result.email).nonEmpty
      )
      .verifying(
        "Incorrect password.",
        result => Account.authenticate(result.email, result.password).nonEmpty
      )
  }
}
