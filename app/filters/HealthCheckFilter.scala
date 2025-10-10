/**
  *
  * @author Rhea Fernandes
  */
package filters;

import commons.DialCodeErrorMessage
import commons.exception.{ResponseCode, ServiceUnavailableException}
import controllers.BaseController
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import play.core.j.JavaHelpers

class HealthCheckFilter(implicit ec: ExecutionContext) extends Filter {
  val baseController :BaseController = new BaseController();
  
  override implicit val mat: akka.stream.Materializer = ???
  
  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    if (!requestHeader.path.contains("/health")) {
      if (!managers.HealthCheckManager.health) {
        val jResult:play.mvc.Result =baseController.getServiceUnavailableResponseEntity(new ServiceUnavailableException(ResponseCode.SERVICE_UNAVAILABLE.code().toString,DialCodeErrorMessage.ERR_SERVICE_UNAVAILABLE),"sunbird.dialcode.exception",null)
        Future{
          jResult.asScala
        }
      } else {
        nextFilter(requestHeader)
      }
    } else {
      nextFilter(requestHeader)
    }
  }
}
