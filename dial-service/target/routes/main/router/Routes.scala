
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kartheek/Documents/Workspace/Sunbird/Sunbird_Fork/sunbird-dial-service/dial-service/conf/routes
// @DATE:Wed Nov 22 12:19:26 IST 2023

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset

object Routes extends Routes

class Routes extends GeneratedRouter {

  import ReverseRouteContext.empty

  override val errorHandler: play.api.http.HttpErrorHandler = play.api.http.LazyHttpErrorHandler

  private var _prefix = "/"

  def withPrefix(prefix: String): Routes = {
    _prefix = prefix
    router.RoutesPrefix.setPrefix(prefix)
    
    this
  }

  def prefix: String = _prefix

  lazy val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation: Seq[(String, String, String)] = List(
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """health""", """@controllers.health.HealthCheckController@.checkSystemHealth()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """service/health""", """@controllers.health.HealthCheckController@.checkServiceHealth()"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/generate""", """@controllers.dialcode.DialcodeV3Controller@.generateDialCode()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/read/$id<[^/]+>""", """@controllers.dialcode.DialcodeV3Controller@.readDialCode(id:String)"""),
    ("""PATCH""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/update/$id<[^/]+>""", """@controllers.dialcode.DialcodeV3Controller@.updateDialCode(id:String)"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/list""", """@controllers.dialcode.DialcodeV3Controller@.listDialCode()"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/search""", """@controllers.dialcode.DialcodeV3Controller@.searchDialCode()"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/sync""", """@controllers.dialcode.DialcodeV3Controller@.syncDialCode()"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/publish/$id<[^/]+>""", """@controllers.dialcode.DialcodeV3Controller@.publishDialCode(id:String)"""),
    ("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/publisher/create""", """@controllers.dialcode.DialcodeV3Controller@.createPublisher()"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/publisher/read/$id<[^/]+>""", """@controllers.dialcode.DialcodeV3Controller@.readPublisher(id:String)"""),
    ("""PATCH""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v3/publisher/update/$id<[^/]+>""", """@controllers.dialcode.DialcodeV3Controller@.updatePublisher(id:String)"""),
    ("""PATCH""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v4/update/$id<[^/]+>""", """@controllers.dialcode.DialcodeV4Controller@.updateDialCode(id:String)"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v4/read/$id<[^/]+>""", """@controllers.dialcode.DialcodeV4Controller@.readDialCode(id:String)"""),
    ("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """dialcode/v4/batch/read/$processid<[^/]+>""", """@controllers.dialcode.DialcodeV4Controller@.readQRCodesBatchInfo(processid:String)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:4
  private[this] lazy val controllers_health_HealthCheckController_checkSystemHealth0_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("health")))
  )
  private[this] lazy val controllers_health_HealthCheckController_checkSystemHealth0_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.health.HealthCheckController]).checkSystemHealth(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.health.HealthCheckController",
      "checkSystemHealth",
      Nil,
      "GET",
      """ Routes
 This file defines all application routes (Higher priority routes first)
 ~~~~""",
      this.prefix + """health"""
    )
  )

  // @LINE:5
  private[this] lazy val controllers_health_HealthCheckController_checkServiceHealth1_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("service/health")))
  )
  private[this] lazy val controllers_health_HealthCheckController_checkServiceHealth1_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.health.HealthCheckController]).checkServiceHealth(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.health.HealthCheckController",
      "checkServiceHealth",
      Nil,
      "GET",
      """""",
      this.prefix + """service/health"""
    )
  )

  // @LINE:6
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_generateDialCode2_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/generate")))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_generateDialCode2_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).generateDialCode(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "generateDialCode",
      Nil,
      "POST",
      """""",
      this.prefix + """dialcode/v3/generate"""
    )
  )

  // @LINE:7
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_readDialCode3_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/read/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_readDialCode3_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).readDialCode(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "readDialCode",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """dialcode/v3/read/$id<[^/]+>"""
    )
  )

  // @LINE:8
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_updateDialCode4_route: Route.ParamsExtractor = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/update/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_updateDialCode4_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).updateDialCode(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "updateDialCode",
      Seq(classOf[String]),
      "PATCH",
      """""",
      this.prefix + """dialcode/v3/update/$id<[^/]+>"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_listDialCode5_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/list")))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_listDialCode5_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).listDialCode(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "listDialCode",
      Nil,
      "POST",
      """""",
      this.prefix + """dialcode/v3/list"""
    )
  )

  // @LINE:10
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_searchDialCode6_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/search")))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_searchDialCode6_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).searchDialCode(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "searchDialCode",
      Nil,
      "POST",
      """""",
      this.prefix + """dialcode/v3/search"""
    )
  )

  // @LINE:11
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_syncDialCode7_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/sync")))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_syncDialCode7_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).syncDialCode(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "syncDialCode",
      Nil,
      "POST",
      """""",
      this.prefix + """dialcode/v3/sync"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_publishDialCode8_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/publish/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_publishDialCode8_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).publishDialCode(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "publishDialCode",
      Seq(classOf[String]),
      "POST",
      """""",
      this.prefix + """dialcode/v3/publish/$id<[^/]+>"""
    )
  )

  // @LINE:13
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_createPublisher9_route: Route.ParamsExtractor = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/publisher/create")))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_createPublisher9_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).createPublisher(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "createPublisher",
      Nil,
      "POST",
      """""",
      this.prefix + """dialcode/v3/publisher/create"""
    )
  )

  // @LINE:14
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_readPublisher10_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/publisher/read/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_readPublisher10_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).readPublisher(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "readPublisher",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """dialcode/v3/publisher/read/$id<[^/]+>"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_updatePublisher11_route: Route.ParamsExtractor = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v3/publisher/update/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV3Controller_updatePublisher11_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).updatePublisher(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV3Controller",
      "updatePublisher",
      Seq(classOf[String]),
      "PATCH",
      """""",
      this.prefix + """dialcode/v3/publisher/update/$id<[^/]+>"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_dialcode_DialcodeV4Controller_updateDialCode12_route: Route.ParamsExtractor = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v4/update/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV4Controller_updateDialCode12_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV4Controller]).updateDialCode(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV4Controller",
      "updateDialCode",
      Seq(classOf[String]),
      "PATCH",
      """""",
      this.prefix + """dialcode/v4/update/$id<[^/]+>"""
    )
  )

  // @LINE:17
  private[this] lazy val controllers_dialcode_DialcodeV4Controller_readDialCode13_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v4/read/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV4Controller_readDialCode13_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV4Controller]).readDialCode(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV4Controller",
      "readDialCode",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """dialcode/v4/read/$id<[^/]+>"""
    )
  )

  // @LINE:18
  private[this] lazy val controllers_dialcode_DialcodeV4Controller_readQRCodesBatchInfo14_route: Route.ParamsExtractor = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("dialcode/v4/batch/read/"), DynamicPart("processid", """[^/]+""",true)))
  )
  private[this] lazy val controllers_dialcode_DialcodeV4Controller_readQRCodesBatchInfo14_invoker = createInvoker(
    play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV4Controller]).readQRCodesBatchInfo(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.dialcode.DialcodeV4Controller",
      "readQRCodesBatchInfo",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """dialcode/v4/batch/read/$processid<[^/]+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:4
    case controllers_health_HealthCheckController_checkSystemHealth0_route(params) =>
      call { 
        controllers_health_HealthCheckController_checkSystemHealth0_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.health.HealthCheckController]).checkSystemHealth())
      }
  
    // @LINE:5
    case controllers_health_HealthCheckController_checkServiceHealth1_route(params) =>
      call { 
        controllers_health_HealthCheckController_checkServiceHealth1_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.health.HealthCheckController]).checkServiceHealth())
      }
  
    // @LINE:6
    case controllers_dialcode_DialcodeV3Controller_generateDialCode2_route(params) =>
      call { 
        controllers_dialcode_DialcodeV3Controller_generateDialCode2_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).generateDialCode())
      }
  
    // @LINE:7
    case controllers_dialcode_DialcodeV3Controller_readDialCode3_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV3Controller_readDialCode3_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).readDialCode(id))
      }
  
    // @LINE:8
    case controllers_dialcode_DialcodeV3Controller_updateDialCode4_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV3Controller_updateDialCode4_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).updateDialCode(id))
      }
  
    // @LINE:9
    case controllers_dialcode_DialcodeV3Controller_listDialCode5_route(params) =>
      call { 
        controllers_dialcode_DialcodeV3Controller_listDialCode5_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).listDialCode())
      }
  
    // @LINE:10
    case controllers_dialcode_DialcodeV3Controller_searchDialCode6_route(params) =>
      call { 
        controllers_dialcode_DialcodeV3Controller_searchDialCode6_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).searchDialCode())
      }
  
    // @LINE:11
    case controllers_dialcode_DialcodeV3Controller_syncDialCode7_route(params) =>
      call { 
        controllers_dialcode_DialcodeV3Controller_syncDialCode7_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).syncDialCode())
      }
  
    // @LINE:12
    case controllers_dialcode_DialcodeV3Controller_publishDialCode8_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV3Controller_publishDialCode8_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).publishDialCode(id))
      }
  
    // @LINE:13
    case controllers_dialcode_DialcodeV3Controller_createPublisher9_route(params) =>
      call { 
        controllers_dialcode_DialcodeV3Controller_createPublisher9_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).createPublisher())
      }
  
    // @LINE:14
    case controllers_dialcode_DialcodeV3Controller_readPublisher10_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV3Controller_readPublisher10_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).readPublisher(id))
      }
  
    // @LINE:15
    case controllers_dialcode_DialcodeV3Controller_updatePublisher11_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV3Controller_updatePublisher11_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV3Controller]).updatePublisher(id))
      }
  
    // @LINE:16
    case controllers_dialcode_DialcodeV4Controller_updateDialCode12_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV4Controller_updateDialCode12_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV4Controller]).updateDialCode(id))
      }
  
    // @LINE:17
    case controllers_dialcode_DialcodeV4Controller_readDialCode13_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_dialcode_DialcodeV4Controller_readDialCode13_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV4Controller]).readDialCode(id))
      }
  
    // @LINE:18
    case controllers_dialcode_DialcodeV4Controller_readQRCodesBatchInfo14_route(params) =>
      call(params.fromPath[String]("processid", None)) { (processid) =>
        controllers_dialcode_DialcodeV4Controller_readQRCodesBatchInfo14_invoker.call(play.api.Play.maybeApplication.map(_.injector).getOrElse(play.api.inject.NewInstanceInjector).instanceOf(classOf[controllers.dialcode.DialcodeV4Controller]).readQRCodesBatchInfo(processid))
      }
  }
}