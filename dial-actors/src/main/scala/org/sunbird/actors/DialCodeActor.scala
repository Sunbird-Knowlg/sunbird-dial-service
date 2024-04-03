package org.sunbird.actors

import org.apache.commons.lang3.StringUtils

import java.util
import javax.inject.Inject
import org.sunbird.actor.core.BaseActor
import org.sunbird.commons.dto.{Request, Response, ResponseHandler}
import org.sunbird.commons.exception.{ClientException, ResponseCode}
import org.sunbird.utils.Constants
import org.sunbird.managers.DialcodeManager

import scala.concurrent.{ExecutionContext, Future}

class DialCodeActor @Inject() extends BaseActor {

  implicit val ec: ExecutionContext = getContext().dispatcher

  override def onReceive(request: Request): Future[Response] = {
    request.getOperation match {
      case Constants.GENERATE_DIALCODE => generateDialCode(request)
      case Constants.READ_DIALCODE => readDialCode(request)
      case Constants.UPDATE_DIALCODE => updateDialCode(request)
      case Constants.LIST_DIALCODE => listDialCode(request)
      case Constants.SEARCH_DIALCODE => searchDialCode(request)
      case Constants.PUBLISH_DIALCODE => publishDialCode(request)
      case Constants.CREATE_PUBLISHER => createPublisher(request)
      case Constants.READ_PUBLISHER => readPublisher(request)
      case Constants.UPDATE_PUBLISHER => updatePublisher(request)
      case Constants.READ_DIALCODE_V4 => readDialCodeV4(request)
      case Constants.UPDATE_DIALCODE_V4 => updateDialCodeV4(request)
      case Constants.READ_QR_CODES_BATCH_INFO => readQRCodesBatchInfo(request)
      case _ => ERROR(request.getOperation)
    }
  }
  private val dialManager = new DialcodeManager

  @throws[Exception]
  private def generateDialCode(request: Request): Future[Response] = {
    val response = dialManager.generateDialCode(request.getRequest, request.getRequest.getOrDefault(Constants.CHANNEL_ID, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def readDialCode(request: Request): Future[Response] = {
    val response = dialManager.readDialCode(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }
  @throws[Exception]
  private def updateDialCode(request: Request): Future[Response] = {
    val response = dialManager.updateDialCode(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String],
      request.getRequest.getOrDefault(Constants.CHANNEL_ID, "").asInstanceOf[String], request.getRequest)
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def listDialCode(request: Request): Future[Response] = {
    val response = dialManager.listDialCode(request.getRequest, request.getRequest.getOrDefault("search", new java.util.HashMap()).asInstanceOf[util.Map[String, AnyRef]])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def searchDialCode(request: Request): Future[Response] = {
    var fieldsList = new util.ArrayList[String]().asInstanceOf[util.List[String]]
    if(request.getRequest.get("fields") != null){
      try {
        fieldsList = request.getRequest.get("fields").asInstanceOf[util.List[String]]
      }
      catch {
        case _: ClassCastException => fieldsList.add(request.getRequest.get("fields").asInstanceOf[String])
      }
      fieldsList.add("identifier")
    }
    val response = dialManager.searchDialCode(request.getRequest, request.getRequest.getOrDefault("search", new java.util.HashMap()).asInstanceOf[util.Map[String, AnyRef]], fieldsList)
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }
  @throws[Exception]
  private def publishDialCode(request: Request): Future[Response] = {
    val response = dialManager.publishDialCode(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String],
      request.getRequest.getOrDefault(Constants.CHANNEL_ID, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def createPublisher(request: Request): Future[Response] = {
    val response = dialManager.createPublisher(request.getRequest, request.getRequest.getOrDefault(Constants.CHANNEL_ID, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else Future {
      ResponseHandler.OK().putAll(response.getResult)

    }
  }

  @throws[Exception]
  private def readPublisher(request: Request): Future[Response] = {
    val response = dialManager.readPublisher(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def updatePublisher(request: Request): Future[Response] = {
    val response = dialManager.updatePublisher(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String],
      request.getRequest.getOrDefault(Constants.CHANNEL_ID, "").asInstanceOf[String], request.getRequest.getOrDefault(Constants.PUBLISHER, "").asInstanceOf[util.Map[String, AnyRef]] )
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def readDialCodeV4(request: Request): Future[Response] = {
    val response = dialManager.readDialCodeV4(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def updateDialCodeV4(request: Request): Future[Response] = {
    val response = dialManager.updateDialCodeV4(request.getRequest.getOrDefault(Constants.IDENTIFIER, "").asInstanceOf[String],
      request.getRequest.getOrDefault(Constants.CHANNEL_ID, "").asInstanceOf[String], request.getRequest)
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }

  @throws[Exception]
  private def readQRCodesBatchInfo(request: Request): Future[Response] = {
    val response = dialManager.readQRCodesBatchInfo(request.getRequest.getOrDefault(Constants.PROCESS_ID, "").asInstanceOf[String])
    if (ResponseHandler.checkError(response)) {
      throw new ClientException(response.getParams.getErr, response.getParams.getErrmsg)
    }
    else {
      Future { ResponseHandler.OK().putAll(response.getResult) }
    }
  }
}