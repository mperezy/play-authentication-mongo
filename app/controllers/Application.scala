package controllers

import forms.{SignInForm, SignUpForm}
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthenticationElement
import models.Account
import reactivemongo.bson.BSONObjectID
import services.AuthConfigImpl
import scala.concurrent.Future

object Application extends Controller with AuthenticationElement with AuthConfigImpl {

  def showSignInForm = Action.async { implicit request =>
    request.session.get("rememberme") match {
      case Some(string) => Future.successful(Redirect(routes.Application.showDashboard()))
      case _ => Future.successful(Ok(views.html.signIn(SignInForm.form, None)))
    }
  }

  def showSignUpForm = Action.async { implicit request =>
    request.session.get("rememberme") match {
      case Some(string) => Future.successful(Redirect(routes.Application.showDashboard()))
      case _ => Future.successful(Ok(views.html.signUp(SignUpForm.form, None)))
    }
  }

  def showDashboard = AsyncStack { implicit request =>
    Future.successful(Ok(views.html.dashboard(Some(loggedIn))))
  }

  def showProfile = AsyncStack { implicit request =>
    Future.successful(Ok(views.html.profile(Some(loggedIn), SignUpForm.form)))
  }
}
