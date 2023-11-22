
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kartheek/Documents/Workspace/Sunbird/Sunbird_Fork/sunbird-dial-service/dial-service/conf/routes
// @DATE:Wed Nov 22 12:19:26 IST 2023

package controllers.dialcode;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.dialcode.ReverseDialcodeV3Controller DialcodeV3Controller = new controllers.dialcode.ReverseDialcodeV3Controller(RoutesPrefix.byNamePrefix());
  public static final controllers.dialcode.ReverseDialcodeV4Controller DialcodeV4Controller = new controllers.dialcode.ReverseDialcodeV4Controller(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.dialcode.javascript.ReverseDialcodeV3Controller DialcodeV3Controller = new controllers.dialcode.javascript.ReverseDialcodeV3Controller(RoutesPrefix.byNamePrefix());
    public static final controllers.dialcode.javascript.ReverseDialcodeV4Controller DialcodeV4Controller = new controllers.dialcode.javascript.ReverseDialcodeV4Controller(RoutesPrefix.byNamePrefix());
  }

}
