
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kartheek/Documents/Workspace/Sunbird/Sunbird_Fork/sunbird-dial-service/dial-service/conf/routes
// @DATE:Wed Nov 22 12:19:26 IST 2023

package controllers.health;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.health.ReverseHealthCheckController HealthCheckController = new controllers.health.ReverseHealthCheckController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.health.javascript.ReverseHealthCheckController HealthCheckController = new controllers.health.javascript.ReverseHealthCheckController(RoutesPrefix.byNamePrefix());
  }

}
