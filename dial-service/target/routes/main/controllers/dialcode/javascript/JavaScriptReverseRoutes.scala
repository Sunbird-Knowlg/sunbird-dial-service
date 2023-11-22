
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kartheek/Documents/Workspace/Sunbird/Sunbird_Fork/sunbird-dial-service/dial-service/conf/routes
// @DATE:Wed Nov 22 12:19:26 IST 2023

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:6
package controllers.dialcode.javascript {
  import ReverseRouteContext.empty

  // @LINE:6
  class ReverseDialcodeV3Controller(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:12
    def publishDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.publishDialCode",
      """
        function(id) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/publish/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:7
    def readDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.readDialCode",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/read/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:11
    def syncDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.syncDialCode",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/sync"})
        }
      """
    )
  
    // @LINE:8
    def updateDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.updateDialCode",
      """
        function(id) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:15
    def updatePublisher: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.updatePublisher",
      """
        function(id) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/publisher/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:6
    def generateDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.generateDialCode",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/generate"})
        }
      """
    )
  
    // @LINE:14
    def readPublisher: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.readPublisher",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/publisher/read/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:9
    def listDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.listDialCode",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/list"})
        }
      """
    )
  
    // @LINE:13
    def createPublisher: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.createPublisher",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/publisher/create"})
        }
      """
    )
  
    // @LINE:10
    def searchDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV3Controller.searchDialCode",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v3/search"})
        }
      """
    )
  
  }

  // @LINE:16
  class ReverseDialcodeV4Controller(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:18
    def readQRCodesBatchInfo: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV4Controller.readQRCodesBatchInfo",
      """
        function(processid) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v4/batch/read/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("processid", encodeURIComponent(processid))})
        }
      """
    )
  
    // @LINE:16
    def updateDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV4Controller.updateDialCode",
      """
        function(id) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v4/update/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
    // @LINE:17
    def readDialCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.dialcode.DialcodeV4Controller.readDialCode",
      """
        function(id) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "dialcode/v4/read/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("id", encodeURIComponent(id))})
        }
      """
    )
  
  }


}