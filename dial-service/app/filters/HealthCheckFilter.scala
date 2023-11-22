/**
  *
  * @author Rhea Fernandes
  */
package filters

import org.sunbird.commons.DialCodeErrorMessage
import org.sunbird.commons.exception.{ResponseCode, ServiceUnavailableException}
import org.sunbird.managers
import controllers.BaseController
import play.api.mvc._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.core.j.JavaHelpers

class HealthCheckFilter extends Filter {
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
