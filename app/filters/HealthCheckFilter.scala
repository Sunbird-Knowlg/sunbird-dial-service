/**
  *
  * @author Rhea Fernandes
  */
package filters;

import akka.actor.TypedActor.dispatcher
import akka.stream.Materializer
import commons.DialCodeErrorMessage
import commons.exception.{ResponseCode, ServiceUnavailableException}
import controllers.BaseController
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ Future }
import play.core.j.JavaHelpers

class HealthCheckFilter @Inject() (implicit val mat: Materializer) extends Filter {
  val baseController :BaseController = new BaseController();
  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    if (!requestHeader.path.contains("/health")) {
      if (!managers.HealthCheckManager.health) {
        val jContext = JavaHelpers.createJavaContext(requestHeader)
        val jResult:play.mvc.Result =baseController.getServiceUnavailableResponseEntity(new ServiceUnavailableException(ResponseCode.SERVICE_UNAVAILABLE.code().toString,DialCodeErrorMessage.ERR_SERVICE_UNAVAILABLE),"sunbird.dialcode.exception",null)
        Future{
          JavaHelpers.createResult(jContext,jResult);
        }
      } else {
        nextFilter(requestHeader)
      }
    } else {
      nextFilter(requestHeader)
    }
  }

}
