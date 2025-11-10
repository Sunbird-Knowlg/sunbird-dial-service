/**
 *
 * @author Rhea Fernandes
 */
package controllers.health;

import commons.dto.Response;
import controllers.BaseController;
import managers.HealthCheckManager;
import play.mvc.Result;
import telemetry.TelemetryManager;

import java.util.concurrent.CompletionStage;

public class HealthCheckController extends BaseController {
    private HealthCheckManager healthCheckManager = new HealthCheckManager();
    private  String apiId = "sunbird.dialcode.health";

    public CompletionStage<Result> checkSystemHealth(){
        try {
            Response response= healthCheckManager.getAllServiceHealth();
            return getResponseEntity(response, apiId, null);
        }catch (Exception e){
            e.printStackTrace();
            TelemetryManager.error("System is Unhealthy, Restart needed",e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public CompletionStage<Result> checkServiceHealth() {
        Response response = healthCheckManager.getServiceHealth();
        return getResponseEntity(response, apiId, null);
    }

}
