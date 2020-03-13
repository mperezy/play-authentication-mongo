package controllers

import controllers.encryption.Encrypter
import forms.SignInForm
import jp.t2v.lab.play2.auth.LoginLogout
import models.Account
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.AuthConfigImpl
import scala.concurrent.Future

object Sessions extends Controller with LoginLogout with AuthConfigImpl {
  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(
      _.flashing("info" -> "You've been logged out")
        .removingFromSession("rememberme")
    )
  }

  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest
      .fold(
        formWithErrors => {
          val redirectRoute = routes.Application.showSignInForm()
          val emailDataMapped = formWithErrors.data.filter(_._1 == "email")

          Future.successful(formWithErrors.globalError match {
            case Some(form) => {
              Redirect(redirectRoute)
                .flashing("danger" -> form.message)
                .withSession(Session(emailDataMapped))
            }
            case None =>
              Redirect(redirectRoute)
                .flashing("danger" -> "Incorrect email format.")
                .withSession("email" -> "")
          })
        },
        user => {
          Account.authenticate(user.email, user.password) match {
            case account =>
              gotoLoginSucceeded(account.get._id)
                .map(_.withSession("rememberme" -> "true"))
          }
        }
      )
  }
}
