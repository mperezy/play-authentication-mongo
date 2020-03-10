package services

import controllers.routes
import jp.t2v.lab.play2.auth._
import models.{Account, Role}
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Redirect
import reactivemongo.bson.BSONObjectID
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}

trait AuthConfigImpl extends AuthConfig {

  type Id = Option[BSONObjectID]
  type Authority = Role
  type User = Account
  val idTag: ClassTag[Id] = classTag[Id]

  def resolveUser(
    id: Id
  )(implicit ctx: ExecutionContext): Future[Option[User]] = {
    Future.successful(Account.findById {
      id match {
        case iD: BSONObjectID        => iD.toString
        case Some(acc: BSONObjectID) => acc.stringify
      }
    })
  }

  def sessionTimeoutInSeconds: Int = 2592000

  def loginSucceeded(
    request: RequestHeader
  )(implicit ctx: ExecutionContext): Future[Result] = {
    val uri = request.session
      .get("access_uri")
      .getOrElse(routes.Application.showDashboard.url.toString)
    Future.successful(
      Redirect(uri)
        .withSession(request.session - "access_uri")
        .flashing("success" -> "You successfuly logged in")
    )
  }

  def logoutSucceeded(
    request: RequestHeader
  )(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.showSignInForm))
  }

  def authenticationFailed(
    request: RequestHeader
  )(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(
      Redirect(routes.Application.showSignInForm)
        .withSession("access_uri" -> request.uri)
    )
  }

  override def authorizationFailed(
    request: RequestHeader,
    user: User,
    authority: Option[Authority]
  )(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.showSignInForm))
  }

  def authorizationFailed(request: RequestHeader)(
    implicit ctx: ExecutionContext
  ): Future[Result] = throw new AssertionError

  def authorize(user: User, authority: Authority)(
    implicit ctx: ExecutionContext
  ): Future[Boolean] = Future.successful {
    var rights = user.role match {
      case Some(right) => right
      case _           => ""
    }

    val currentRole = Role.stringValueOf(authority)
    println(s"Current user in role is => $currentRole, user rights $rights")

    (rights, currentRole) match {
      case ("NormalUser", "NormalUser") => true
      case ("Administrator", _)         => true
      case _                            => false
    }
  }

  override lazy val tokenAccessor = new CustomCookieAccessor(
    sessionTimeoutInSeconds
  )
}
