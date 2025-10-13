package controllers.dialcode;

import commons.dto.Request;
import commons.dto.Response;
import controllers.BaseController;
import managers.DialcodeManager;
import java.util.concurrent.CompletionStage;
import play.mvc.Result;
import telemetry.TelemetryManager;
import utils.DialCodeEnum;
import play.mvc.Http;

import java.util.*;

public class DialcodeV4Controller extends BaseController {
    
    private DialcodeManager dialCodeManager = new DialcodeManager();

    public CompletionStage<Result> readDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.read";
        try {
            Response response = dialCodeManager.readDialCodeV4(dialCodeId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occurred while reading Dial Code details : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public CompletionStage<Result> updateDialCode(Http.Request request, String dialCodeId) {
        setCurrentRequest(request);
        String apiId = "sunbird.dialcode.update";
        String channelId = request.header("X-Channel-ID").orElse(null);
        Request req = getRequest(request);
        try {
            Map<String, Object> map = (Map<String, Object>) req.get(DialCodeEnum.dialcode.name());
            Response response = dialCodeManager.updateDialCodeV4(dialCodeId, channelId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
           TelemetryManager.error("Exception Occurred while updating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public CompletionStage<Result> readQRCodesBatchInfo(String processId) {
        String apiId = "sunbird.dialcode.batch.read";
        try {
            Response response = dialCodeManager.readQRCodesBatchInfo(processId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occurred while reading QR ZIP path : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

}
