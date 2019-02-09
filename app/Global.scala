import play.api.mvc._
import play.api.mvc.Results._
import play.api.{Application, GlobalSettings, Logger}
import play.filters.csrf._
import scala.concurrent.Future

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    Logger.info(s"onError: ${ex.printStackTrace()}")
    Future.successful(InternalServerError(views.html.errors.internalError(ex)))
  }

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Logger.info(s"onHandlerNotFound: ${request.headers.toString()}")
    Future.successful(NotFound(views.html.errors.notFound(request)))
  }
}
