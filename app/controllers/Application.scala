package controllers

import forms.{SignInForm, SignUpForm}
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.Account
import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import services.AuthConfigImpl

import scala.concurrent.Future

object Application extends Controller with AuthenticationElement with AuthConfigImpl {

  val emptyAccount = new Account(Some(BSONObjectID.generate), Some(""), "", Some(""), Some(""), "", Some(""), Some(new DateTime()))

  def showSignInForm = Action.async { implicit request =>
    request.session.get("rememberme") match {
      case Some(string) => Future.successful(Redirect(routes.Application.showDashboard()))
      case _ => Future.successful(Ok(views.html.signIn(SignInForm.form, emptyAccount)))
    }
  }

  def showSignUpForm = Action.async { implicit request =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form, emptyAccount)))
  }

  def showDashboard = StackAction { implicit request =>
    val user: User = loggedIn
    Ok(views.html.dashboard(user))
  }
}
