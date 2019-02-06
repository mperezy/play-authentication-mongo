package controllers

import forms.SignInForm
import jp.t2v.lab.play2.auth.LoginLogout
import models.Account
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import services.AuthConfigImpl

import scala.concurrent.Future

object Sessions extends Controller with LoginLogout with AuthConfigImpl {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  import models.Account._

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "success" -> "You've been logged out"
    ).removingFromSession("rememberme"))
  }

  def authenticate = Action.async { implicit request => 
      SignInForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.signIn(formWithErrors))),
        user => Account.authenticate(user.get.email, user.get.password) match {
          case account => {
            //TODO always set cookie to be refactored
            val req = request.copy(tags = request.tags + ("rememberme" -> "true"))
            gotoLoginSucceeded(account.get._id)(req, defaultContext).map(_.withSession("rememberme" -> "true"))
          }
          case _ => Future.successful(BadRequest(Json.obj("result" -> "Couldn't get user from db")))
        }
      )
  }
}
