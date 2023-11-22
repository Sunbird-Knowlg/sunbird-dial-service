
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kartheek/Documents/Workspace/Sunbird/Sunbird_Fork/sunbird-dial-service/dial-service/conf/routes
// @DATE:Wed Nov 22 12:19:26 IST 2023

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:4
package controllers.health.javascript {
  import ReverseRouteContext.empty

  // @LINE:4
  class ReverseHealthCheckController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:5
    def checkServiceHealth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.health.HealthCheckController.checkServiceHealth",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "service/health"})
        }
      """
    )
  
    // @LINE:4
    def checkSystemHealth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.health.HealthCheckController.checkSystemHealth",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "health"})
        }
      """
    )
  
  }


}