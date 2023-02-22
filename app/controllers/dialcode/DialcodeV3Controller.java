package controllers.dialcode;

import commons.dto.Request;
import commons.dto.Response;
import controllers.BaseController;
import managers.DialcodeManager;
import play.libs.F.Promise;
import play.mvc.Result;
import telemetry.TelemetryManager;
import utils.DialCodeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DialcodeV3Controller extends BaseController {
    
    private DialcodeManager dialCodeManager = new DialcodeManager();

    public Promise<Result> generateDialCode() {
        String apiId = "sunbird.dialcode.generate";
        String channelId = request().getHeader("X-Channel-ID");
        Request request = getRequest();
        try {
            Map<String, Object> map = (Map<String, Object>) request.get(DialCodeEnum.dialcodes.name());
            Response response = dialCodeManager.generateDialCode(map, channelId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while generating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public Promise<Result> readDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.read";
        try {
            Response response = dialCodeManager.readDialCode(dialCodeId);
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
            Response response = dialCodeManager.updateDialCode(dialCodeId, channelId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
           TelemetryManager.error("Exception Occured while updating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public Promise<Result> listDialCode() {
        String apiId = "sunbird.dialcode.list";
        Request request = getRequest();
        try {
            Map<String, Object> requestMap = (Map<String, Object>) request.get(DialCodeEnum.search.name());
            Response response = dialCodeManager.listDialCode(request.getContext(), requestMap);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while Performing List Operation for Dial Codes : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public Promise<Result> searchDialCode() {
        String apiId = "sunbird.dialcode.search";
        Request request = getRequest();
        try {
            Map<String, Object> map = (Map<String, Object>) request.get(DialCodeEnum.search.name());
            List<String> fieldsList = null;
            if(request.get(DialCodeEnum.fields.name()) != null) {
                fieldsList = (List<String>) request.get(DialCodeEnum.fields.name());
                fieldsList.add("identifier");
            }
            Response response = dialCodeManager.searchDialCode(request.getContext(), map, fieldsList);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager
                    .error("Exception Occured while Performing Search Operation for Dial Codes : " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    /**
     * @return
     */
    public Promise<Result> syncDialCode() {
        String apiId = "sunbird.dialcode.sync";
        String channelId = request().getHeader("X-Channel-ID");
        String[] ids = request().queryString().get("identifier");
        List<String> identifiers = Optional.ofNullable(Arrays.asList(ids)).orElse(new ArrayList<>());
        Request request = getRequest();
        try {
            Map<String, Object> map = (Map<String, Object>) request.get("sync");
            Response response = dialCodeManager.syncDialCode(channelId, map, identifiers);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager
                    .error("Exception Occured while Performing Sync Operation for Dial Codes : " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    /**
     * @param dialCodeId
     * @return
     */
    public Promise<Result> publishDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.publish";
        String channelId = request().getHeader("X-Channel-ID");
        try {
            Response response = dialCodeManager.publishDialCode(dialCodeId, channelId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while Performing Publish Operation on Dial Code : "+ e.getMessage(),
                    e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    /**
     * Create Publisher.
     *
     * @return
     */

    public Promise<Result> createPublisher() {

        String apiId = "sunbird.publisher.create";
        String channelId = request().getHeader("X-Channel-ID");
        Request request = getRequest();
        try {
            Map<String, Object> map = (Map<String, Object>) request.get(DialCodeEnum.publisher.name());
            Response response = dialCodeManager.createPublisher(map, channelId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while creating Publisher  : " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    /**
     * Read Publisher Details
     *
     * @param publisherId
     *
     * @return
     */
    public Promise<Result> readPublisher(String publisherId) {
        String apiId = "sunbird.publisher.info";
        try {
            Response response = dialCodeManager.readPublisher(publisherId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while reading Publisher details : " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    /**
     * Update Publisher Details
     *
     * @param publisherId
     * @return
     */
    public Promise<Result> updatePublisher(String publisherId) {
        String apiId = "sunbird.publisher.update";
        String channelId = request().getHeader("X-Channel-ID");
        Request request = getRequest();
        try {
            Map<String, Object> map = (Map<String, Object>) request.get("publisher");
            Response response = dialCodeManager.updatePublisher(publisherId, channelId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while updating Publisher : " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }


}
