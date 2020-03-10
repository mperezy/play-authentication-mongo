package controllers

import controllers.Sessions.gotoLoginSucceeded
import forms.SignUpForm
import play.api.Play
import play.api.mvc._
import play.api.libs.json.Json
import play.api.mvc.Controller
import models.Account
import reactivemongo.core.protocol.QueryFlags
import reactivemongo.api.QueryOpts
import jp.t2v.lab.play2.auth.AuthElement
import models.Role.{Admin, Normal}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import services.AuthConfigImpl
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.MongoController
import org.slf4j.{Logger, LoggerFactory}
import reactivemongo.api.Cursor
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import controllers.encryption.Encrypter
import controllers.formerrorconvert.ConvertToFlashing
import controllers.splitter.FlashSplitter

object AccountsCtrl
    extends Controller
    with MongoController
    with AuthElement
    with AuthConfigImpl {
  private final val logger: Logger = LoggerFactory.getLogger(classOf[Account])

  def collection: JSONCollection = db.collection[JSONCollection]("accounts")

  import models.Account._

  def adminCheck = StackAction(AuthorityKey -> Admin) { implicit request =>
    loggedIn match {
      case user: Account => Ok(Json.obj("user" -> user))
      case _             => Ok(Json.obj("user" -> "there is no current user found"))
    }
  }

  //TODO remove password from the user
  def currentUser = AsyncStack(AuthorityKey -> Admin, AuthorityKey -> Normal) {
    implicit request =>
      loggedIn match {
        case user: Account => {
          val updatedUser = user.copy(password = "Well Tried friend");
          Future.successful(Ok(Json.obj("user" -> updatedUser)))
        }
        case _ =>
          Future.successful(
            Ok(Json.obj("result" -> "there is no current user found"))
          )
      }
  }

  def modifyUser = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      formwithErrors => Future.successful(BadRequest("invidalid json")),
      updateUser => {
        Await.result(Account.update(updateUser).map { lastError =>
          println(s"Successfully updated with lastError: $lastError")
        }, 1.seconds)
        Future.successful(
          Redirect(routes.Application.showDashboard()) flashing ("success" -> s"Your data has been successfully updated.")
        )
      }
    )
  }

  //TODO implement search user by regex with search form
  // page must start at 1
  def getUsers(page: Int, numberByPage: Int) = AsyncStack() {
    implicit request =>
      val cursor: Cursor[Account] = collection
        .find(Json.obj())
        .options(
          QueryOpts((page - 1) * numberByPage, numberByPage, QueryFlags.Exhaust)
        )
        .cursor[Account]
      val futureUserList: Future[List[Account]] = cursor.collect[List]()
      val futureUsersJsonArray: Future[JsArray] = futureUserList.map { users =>
        Json.arr(users)
      }
      futureUsersJsonArray.map { users =>
        Ok(users(0))
      }
  }

  def byAtelier(id: String) =
    AsyncStack(AuthorityKey -> Admin, AuthorityKey -> Normal) {
      implicit request =>
        val cursor: Cursor[Account] =
          collection.find(Json.obj("ateliers" -> id)).cursor[Account]
        val futureUserList: Future[Array[Account]] = cursor.collect[Array]()
        val futureUsersJsonArray: Future[JsArray] = futureUserList.map {
          users =>
            if (loggedIn.role.getOrElse("NormalUser") != "Administrator") {
              for (i <- 0 until users.length) {
                users(i) = users(i).copy(password = "Well Tried friend");
              }
            }
            Json.arr(users)
        }
        futureUsersJsonArray.map { users =>
          Ok(users(0))
        }
    }

  def byId(id: String) =
    AsyncStack(AuthorityKey -> Admin, AuthorityKey -> Normal) {
      implicit request =>
        val cursor: Cursor[Account] =
          collection.find(Json.obj("_id" -> BSONObjectID(id))).cursor[Account]
        val futureUserList: Future[Array[Account]] = cursor.collect[Array]()
        val futureUsersJsonArray: Future[JsArray] = futureUserList.map {
          users =>
            if (loggedIn.role.getOrElse("NormalUser") != "Administrator") {
              for (i <- 0 until users.length) {
                users(i) = users(i).copy(password = "Well Tried friend");
              }
            }
            Json.arr(users)
        }
        futureUsersJsonArray.map { users =>
          Ok(users(0))
        }
    }

  def createUser = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest
      .fold(
        formWithErrors => {
          val retrievedFlashMessage =
            ConvertToFlashing.convert(formWithErrors)
          val flashMessage =
            FlashSplitter.split(true, retrievedFlashMessage, 0)
          val userNameFromFlashMessage = FlashSplitter.split(
            false,
            FlashSplitter.split(true, retrievedFlashMessage, 1),
            0
          )
          val userLastNameFromFlashMessage = FlashSplitter.split(
            false,
            FlashSplitter.split(true, retrievedFlashMessage, 1),
            1
          )
          val userEmailFlashMessage = FlashSplitter.split(
            false,
            FlashSplitter.split(true, retrievedFlashMessage, 1),
            2
          )

          Future.successful(
            Redirect(routes.Application.showSignUpForm()) flashing ("danger" -> flashMessage) withSession ("firstname" -> userNameFromFlashMessage, "lastname" -> userLastNameFromFlashMessage, "useremail" -> userEmailFlashMessage)
          )
        },
        newUser => {
          Account.findByEmail(newUser.email) match {
            case Some(account: Account) =>
              Future.successful(
                Redirect(routes.Application.showSignUpForm()).flashing(
                  "danger" -> s"The user with ${newUser.email} is already exists."
                )
              )
            case _ => {
              Await.result(Account.create(newUser).map { lastError =>
                println(
                  s"successfully inserted the user: ${newUser.firstName} with LastError: $lastError"
                )
              }, 1.seconds)
              Account.authenticate(newUser.email, newUser.password) match {
                case account =>
                  gotoLoginSucceeded(account.get._id)
                    .map(_.withSession("rememberme" -> "true"))
              }
            }
          }
        }
      )
  }

  def uploadAvatar(user_id: String) =
    AsyncStack(
      parse.multipartFormData,
      AuthorityKey -> Admin,
      AuthorityKey -> Normal
    ) { implicit request =>
      request.body
        .file("file")
        .map { file =>
          loggedIn match {
            case user if (user._id.get.stringify == user_id) => {
              import java.io.File
              val filename = file.filename.replace(' ', '_')
              val filePath = Play.current.path.getPath + "/avatar/" + user_id + "/" + filename;
              val fileUrl = "logos/avatar/" + user_id + "/" + filename;
              val contentType = file.contentType
              //val title = request.body.asFormUrlEncoded.get("title").get(0);
              val dir =
                new File(Play.current.path.getPath + "/avatar/" + user_id)
              dir.mkdirs()
              file.ref.moveTo(new File(filePath))
              val updatedUser =
                BSONDocument("$set" -> BSONDocument("avatar_path" -> fileUrl))

              val selec = BSONDocument("_id" -> BSONObjectID(user_id));
              collection.update(selec, updatedUser, upsert = true).map {
                lastError =>
                  logger
                    .debug(s"successfully inserted with LastError: $lastError")
                  Created(s"user avatar successfully updated")
              }
            }
            case _ => {
              Future.successful(
                InternalServerError(
                  Json.obj(
                    "result" -> s"your not authorized to modify this entity"
                  )
                )
              )
            }
          }
        }
        .getOrElse {
          Future.successful(
            BadRequest(
              Json.obj("result" -> "user error: missing file or existing file")
            )
          )
        }
    }

  def removeByIds(ids: List[String]) = AsyncStack(AuthorityKey -> Admin) {
    implicit request =>
      val bsonIds = ids.map(id => Json.obj("$oid" -> id))
      val query = Json.obj("_id" -> Json.obj("$in" -> bsonIds))
      collection.remove(query).map { lastError =>
        lastError.inError match {
          case true => {
            logger.debug(s"error while removing user(s) : $lastError")
            InternalServerError(s"error at while removing user(s) : $lastError")
          }
          case false => {
            Ok(s"User(s) Removal Success: $lastError")
          }
        }
      }
  }
}
