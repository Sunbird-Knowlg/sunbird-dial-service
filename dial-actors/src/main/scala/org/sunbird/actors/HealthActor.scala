package org.sunbird.actors

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import org.sunbird.actor.core.BaseActor
import org.sunbird.commons.dto.{Request, Response}
import org.sunbird.managers.HealthCheckManager

@Named("healthActor")
class HealthActor @Inject() (implicit oec: ExecutionContext) extends BaseActor {

  implicit val ec: ExecutionContext = getContext().dispatcher

  @throws[Throwable]
  override def onReceive(request: Request): Future[Response] = {
    Future {
      new HealthCheckManager().getAllServiceHealth
    }(ec)
  }
}
