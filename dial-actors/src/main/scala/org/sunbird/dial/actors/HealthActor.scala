package org.sunbird.dial.actors

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import org.sunbird.actor.core.BaseActor
import org.sunbird.commons.dto.{Request, Response}
import org.sunbird.managers.HealthCheckManager

class HealthActor @Inject() (implicit oec: ExecutionContext) extends BaseActor {

  implicit val ec: ExecutionContext = getContext().dispatcher

  @throws[Throwable]
  override def onReceive(request: Request): Response = {
    new HealthCheckManager().getAllServiceHealth()
  }
}
