package controllers.v4
import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Singleton
import controllers.BaseController
import org.sunbird.utils.Constants
import play.api.mvc.ControllerComponents
import utils.{ActorNames, ApiId}

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._

@Singleton
class DialCodeController @Inject()(@Named(ActorNames.DIALCODE_ACTOR) dialCodeActor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem) (implicit exec: ExecutionContext) extends BaseController(cc) {
  val objectType = "DialCode"
  def readDialCode(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val dialCodes = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    dialCodes.putAll(headers)
    dialCodes.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val dialRequest = getRequest(dialCodes, headers, Constants.READ_DIALCODE_V4)
    setRequestContext(dialRequest, Constants.SCHEMA_VERSION, objectType, Constants.SCHEMA_NAME)
    getResult(ApiId.DIALCODE_READ, dialCodeActor, dialRequest)
  }

  def updateDialCode(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val dialCode = body.getOrDefault(Constants.DIALCODE, new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]]
    dialCode.putAll(headers)
    dialCode.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val dialRequest = getRequest(dialCode, headers, Constants.UPDATE_DIALCODE_V4)
    setRequestContext(dialRequest, Constants.SCHEMA_VERSION, objectType, Constants.SCHEMA_NAME)
    getResult(ApiId.DIALCODE_UPDATE, dialCodeActor, dialRequest)
  }

  def readQRCodesBatchInfo(processId: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    body.putAll(headers)
    body.putAll(Map(Constants.PROCESS_ID -> processId).asJava)
    val dialRequest = getRequest(body, headers, Constants.READ_QR_CODES_BATCH_INFO)
    setRequestContext(dialRequest, Constants.SCHEMA_VERSION, objectType, Constants.SCHEMA_NAME)
    getResult(ApiId.READ_QR_CODES_BATCH, dialCodeActor, dialRequest)
  }

}
