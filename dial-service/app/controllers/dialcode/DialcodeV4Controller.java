package controllers.dialcode;

import org.sunbird.commons.dto.Request;
import org.sunbird.commons.dto.Response;
import controllers.BaseController;
import org.sunbird.managers.DialcodeManager;
import play.libs.F.Promise;
import play.mvc.Result;
import org.sunbird.telemetry.TelemetryManager;
import org.sunbird.utils.DialCodeEnum;

import java.util.*;

public class DialcodeV4Controller extends BaseController {
    
    private DialcodeManager dialCodeManager = new DialcodeManager();

    public Promise<Result> readDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.read";
        try {
            Response response = dialCodeManager.readDialCodeV4(dialCodeId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occurred while reading Dial Code details : "+ e.getMessage(), e);
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
           TelemetryManager.error("Exception Occurred while updating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public Promise<Result> readQRCodesBatchInfo(String processId) {
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
