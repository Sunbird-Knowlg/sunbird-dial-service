package controllers.dialcode;

import commons.dto.Request;
import commons.dto.Response;
import controllers.BaseController;
import managers.DialcodeManager;
import play.libs.F.Promise;
import play.mvc.Result;
import telemetry.TelemetryManager;
import utils.DialCodeEnum;

import java.util.*;

public class DialcodeV4Controller extends BaseController {
    
    private DialcodeManager dialCodeManager = new DialcodeManager();

    public Promise<Result> readDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.read";
        try {
            Response response = dialCodeManager.readDialCodeV4(dialCodeId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while reading Dial Code details : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public Promise<Result> updateDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.update";
        String channelId = request().getHeader("X-Channel-ID");
        Request request = getRequest();
        try {
            Map<String, Object> map = (Map<String, Object>) request.get(DialCodeEnum.dialcode.name());
            Response response = dialCodeManager.updateDialCodeV4(dialCodeId, channelId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
           TelemetryManager.error("Exception Occured while updating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

}
