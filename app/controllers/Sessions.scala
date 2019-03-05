package controllers

import forms.SignInForm
import jp.t2v.lab.play2.auth.LoginLogout
import models.Account
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.AuthConfigImpl
import scala.concurrent.Future
import controllers.Encryption.Encrypter
import controllers.FormErrorConvert.ConvertToFlashing

object Sessions extends Controller with LoginLogout with AuthConfigImpl {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  def splitter(flashMessage: String, index: Int) = {
    if(flashMessage.contains("#")) {
      flashMessage.split("#").toList(index)
    } else {
      if(index == 1) {
        ""
      } else {
        flashMessage
      }
    }
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "info" -> "You've been logged out"
    ).removingFromSession("rememberme"))
  }

  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      formWithErrors => {
        val retrievedFlashMessage = ConvertToFlashing.convertionFormLoginAccount(formWithErrors)
        val flashMessage = splitter(retrievedFlashMessage, 0)
        val emailFromFlashMessage = splitter(retrievedFlashMessage, 1)

        Future.successful(Redirect(routes.Application.showSignInForm()) flashing("danger" -> flashMessage) withSession("email" -> emailFromFlashMessage))
      },
      user => {
        Account.findByEmail(user.email) match {
          case Some(account: Account) => {
            Account.authenticate(user.email, user.password) match {
              case account =>
                val req = request.copy(tags = request.tags + ("rememberme" -> "true"))
                gotoLoginSucceeded(account.get._id)(req, defaultContext).map(_.withSession("rememberme" -> "true"))
            }
          }
        }
      }
    )
  }
}
