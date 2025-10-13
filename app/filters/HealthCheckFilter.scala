/**
  *
  * @author Rhea Fernandes
  */
package filters

import commons.DialCodeErrorMessage
import commons.exception.{ResponseCode, ServiceUnavailableException}
import controllers.BaseController
import play.api.mvc._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import akka.stream.Materializer

class HealthCheckFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  val baseController :BaseController = new BaseController()
  
  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    if (!requestHeader.path.contains("/health")) {
      if (!managers.HealthCheckManager.health) {
        val jResult:play.mvc.Result = baseController.getServiceUnavailableResponseEntity(
          new ServiceUnavailableException(ResponseCode.SERVICE_UNAVAILABLE.code().toString, DialCodeErrorMessage.ERR_SERVICE_UNAVAILABLE),
          "sunbird.dialcode.exception", 
          null
        )
        Future.successful(jResult.asScala)
      } else {
        nextFilter(requestHeader)
      }
    } else {
      nextFilter(requestHeader)
    }
  }
}
