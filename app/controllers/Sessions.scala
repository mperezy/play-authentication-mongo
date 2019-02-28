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

object Sessions extends Controller with LoginLogout with AuthConfigImpl {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "info" -> "You've been logged out"
    ).removingFromSession("rememberme"))
  }

  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      formWithErrors => Future.successful(Redirect(routes.Application.showSignInForm()).flashing("danger" -> "Those credentials are not valid.")),
      user => {
        Account.authenticate(user.get.email, Encrypter.decrypt(user.get.password)) match {
        case Some(account: Account) =>
          //TODO always set cookie to be refactored
          val req = request.copy(tags = request.tags + ("rememberme" -> "true"))
          gotoLoginSucceeded(account._id)(req, defaultContext).map(_.withSession("rememberme" -> "true"))
        case None => Future.successful(Redirect(routes.Application.showSignInForm()).flashing("danger" -> s"We're sorry, that user is not exists."))
      }}
    )
  }
}
