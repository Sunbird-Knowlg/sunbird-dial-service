package org.sunbird.actors

import org.apache.commons.lang3.StringUtils

import java.util
import javax.inject.Inject
import org.sunbird.actor.core.BaseActor
import org.sunbird.commons.dto.{Request, Response}

import scala.concurrent.{ExecutionContext, Future}

class DialCodeActor @Inject()(implicit exec: ExecutionContext) extends BaseActor {

  implicit val ec: ExecutionContext = getContext().dispatcher

  override def onReceive(request: Request): Future[Response] = {
    request.getOperation match {
      case _ => ERROR(request.getOperation)
    }
  }

}