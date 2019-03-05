package forms

import play.api.data.Form
import play.api.data.Forms._
import models.Account

object SignInForm {

  case class LoginAccount(email : String, password : String)

  var incorrectPasswordErrorMessage = ""
  val form = Form {
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(LoginAccount.apply)(LoginAccount.unapply)
    .verifying("That email is not registered.", result => isUserExists(result.email))
    .verifying(incorrectPasswordErrorMessage, result => isCorrectPassword(result.email, result.password))
  }

  def isUserExists(email: String): Boolean = {
    Account.findByEmail(email) match {
      case Some(account: Account) => {
        true
      }
      case _ => {
        false
      }
    }
  }

  def isCorrectPassword(email: String, password: String) = {
    Account.authenticate(email, password) match {
      case Some(account: Account) => {
        true
      }
      case _ => {
        incorrectPasswordErrorMessage = s"Incorrect password.#${email}"
        false
      }
    }
  }
}
