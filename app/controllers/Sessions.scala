package controllers

import forms.SignInForm
import jp.t2v.lab.play2.auth.LoginLogout
import models.Account
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.AuthConfigImpl
import scala.concurrent.Future
import controllers.encryption.Encrypter
import controllers.formerrorconvert.ConvertToFlashing
import controllers.splitter.FlashSplitter

object Sessions extends Controller with LoginLogout with AuthConfigImpl {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "info" -> "You've been logged out"
    ).removingFromSession("rememberme"))
  }

  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      formWithErrors => {
        val retrievedFlashMessage = ConvertToFlashing.convertionFormLoginAccount(formWithErrors)
        val flashMessage = FlashSplitter.flashMessageSplitted(retrievedFlashMessage, 0)
        val emailFromFlashMessage = FlashSplitter.flashMessageSplitted(retrievedFlashMessage, 1)

        Future.successful(Redirect(routes.Application.showSignInForm()) flashing("danger" -> flashMessage) withSession("email" -> emailFromFlashMessage))
      },
      user => {
        Account.authenticate(user.email, user.password) match {
          case account =>
            gotoLoginSucceeded(account.get._id).map(_.withSession("rememberme" -> "true"))
        }
      }
    )
  }
}
