
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kartheek/Documents/Workspace/Sunbird/Sunbird_Fork/sunbird-dial-service/dial-service/conf/routes
// @DATE:Wed Nov 22 12:19:26 IST 2023

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset

// @LINE:6
package controllers.dialcode {

  // @LINE:6
  class ReverseDialcodeV3Controller(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:12
    def publishDialCode(id:String): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "dialcode/v3/publish/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:7
    def readDialCode(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "dialcode/v3/read/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:11
    def syncDialCode(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "dialcode/v3/sync")
    }
  
    // @LINE:8
    def updateDialCode(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PATCH", _prefix + { _defaultPrefix } + "dialcode/v3/update/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:15
    def updatePublisher(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PATCH", _prefix + { _defaultPrefix } + "dialcode/v3/publisher/update/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:6
    def generateDialCode(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "dialcode/v3/generate")
    }
  
    // @LINE:14
    def readPublisher(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "dialcode/v3/publisher/read/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:9
    def listDialCode(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "dialcode/v3/list")
    }
  
    // @LINE:13
    def createPublisher(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "dialcode/v3/publisher/create")
    }
  
    // @LINE:10
    def searchDialCode(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "dialcode/v3/search")
    }
  
  }

  // @LINE:16
  class ReverseDialcodeV4Controller(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:18
    def readQRCodesBatchInfo(processid:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "dialcode/v4/batch/read/" + implicitly[PathBindable[String]].unbind("processid", dynamicString(processid)))
    }
  
    // @LINE:16
    def updateDialCode(id:String): Call = {
      import ReverseRouteContext.empty
      Call("PATCH", _prefix + { _defaultPrefix } + "dialcode/v4/update/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
    // @LINE:17
    def readDialCode(id:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "dialcode/v4/read/" + implicitly[PathBindable[String]].unbind("id", dynamicString(id)))
    }
  
  }


}