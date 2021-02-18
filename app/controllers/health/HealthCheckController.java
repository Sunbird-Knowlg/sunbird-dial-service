/**
 *
 * @author Rhea Fernandes
 */
package controllers.health;

import commons.dto.Response;
import controllers.BaseController;
import managers.HealthCheckManager;
import play.libs.F.Promise;
import play.mvc.Result;
import telemetry.TelemetryManager;

public class HealthCheckController extends BaseController {
    private HealthCheckManager healthCheckManager = new HealthCheckManager();
    private  String apiId = "sunbird.dialcode.health";

    public Promise<Result> checkSystemHealth(){
        try {
            Response response= healthCheckManager.getAllServiceHealth();
            return getResponseEntity(response, apiId, null);
        }catch (Exception e){
            e.printStackTrace();
            TelemetryManager.error("System is Unhealthy, Restart needed",e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public Promise<Result> checkServiceHealth() {
        Response response = healthCheckManager.getServiceHealth();
        return getResponseEntity(response, apiId, null);
    }

}
