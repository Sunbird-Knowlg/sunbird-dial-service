package modules

import com.google.inject.AbstractModule
import org.sunbird.actors.{DialCodeActor, HealthActor}
import play.libs.akka.AkkaGuiceSupport
import utils.ActorNames
class DialModule extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    super.configure()
    bindActor(classOf[HealthActor], ActorNames.HEALTH_ACTOR)
    bindActor(classOf[DialCodeActor], ActorNames.DIALCODE_ACTOR)
    println("Initialized application actors for DIAL service")
  }
}
