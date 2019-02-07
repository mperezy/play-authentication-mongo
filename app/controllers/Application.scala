package controllers

import forms.{SignInForm, SignUpForm}
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthenticationElement
import services.AuthConfigImpl

import scala.concurrent.Future

object Application extends Controller with AuthenticationElement with AuthConfigImpl {

  def showSignInForm = Action.async { implicit request =>
    request.session.get("rememberme") match {
      case Some(string) => Future.successful(Redirect(routes.Application.showDashboard()))
      case _ => Future.successful(Ok(views.html.signIn(SignInForm.form)))
    }
  }

  def showSignUpForm = Action.async { implicit request =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  def showDashboard = StackAction { implicit request =>
    val user: User = loggedIn
    Ok(views.html.dashboard(user))
  }
}
