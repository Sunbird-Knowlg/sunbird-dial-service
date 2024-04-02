package controllers.v3

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

  def generateDialCode() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val dialCodes = body.getOrDefault(Constants.DIALCODES, new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]]
    dialCodes.putAll(headers)
    val dialRequest = getRequest(dialCodes, headers, Constants.GENERATE_DIALCODE)
    setRequestContext(dialRequest, "1.0", "DialCode", "dialcode")
    getResult(ApiId.DIALCODE_GENERATE, dialCodeActor, dialRequest)
  }

  def readDialCode(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val dialCodes = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    dialCodes.putAll(headers)
    dialCodes.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val dialRequest = getRequest(dialCodes, headers, Constants.READ_DIALCODE)
    getResult(ApiId.DIALCODE_READ, dialCodeActor, dialRequest)
  }
  def updateDialCode(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val dialCode = body.getOrDefault(Constants.DIALCODE, new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]]
    dialCode.putAll(headers)
    dialCode.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val dialRequest = getRequest(dialCode, headers, Constants.UPDATE_DIALCODE)
    getResult(ApiId.DIALCODE_UPDATE, dialCodeActor, dialRequest)
  }

  def listDialCode() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    body.putAll(headers)
    val dialRequest = getRequest(body, headers, Constants.LIST_DIALCODE)
    getResult(ApiId.DIALCODE_LIST, dialCodeActor, dialRequest)
  }

  def searchDialCode() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    body.putAll(headers)
    val dialRequest = getRequest(body, headers, Constants.SEARCH_DIALCODE)
    getResult(ApiId.DIALCODE_SEARCH, dialCodeActor, dialRequest)
  }

  def syncDialCode() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    body.putAll(headers)
    val dialRequest = getRequest(body, headers, Constants.SYNC_DIALCODE)
    getResult(ApiId.DIALCODE_SYNC, dialCodeActor, dialRequest)
  }

  def publishDialCode(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val dialCodes = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    dialCodes.putAll(headers)
    dialCodes.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val dialRequest = getRequest(dialCodes, headers, Constants.PUBLISH_DIALCODE)
    getResult(ApiId.DIALCODE_PUBLISH, dialCodeActor, dialRequest)
  }

  def createPublisher() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val publisher = body.getOrDefault(Constants.PUBLISHER, new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]]
    publisher.putAll(headers)
    val publisherRequest = getRequest(publisher, headers, Constants.CREATE_PUBLISHER)
    setRequestContext(publisherRequest, "1.0", "DialCode", "dialcode")
    getResult(ApiId.PUBLISHER_CREATE, dialCodeActor, publisherRequest)
  }

  def readPublisher(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val publisher = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    publisher.putAll(headers)
    publisher.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val publisherRequest = getRequest(publisher, headers, Constants.READ_PUBLISHER)
    getResult(ApiId.PUBLISHER_READ, dialCodeActor, publisherRequest)
  }

  def updatePublisher(id: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    body.putAll(headers)
    body.putAll(Map(Constants.IDENTIFIER -> id).asJava)
    val publisherRequest = getRequest(body, headers, Constants.UPDATE_PUBLISHER)
    getResult(ApiId.PUBLISHER_UPDATE, dialCodeActor, publisherRequest)
  }

}
