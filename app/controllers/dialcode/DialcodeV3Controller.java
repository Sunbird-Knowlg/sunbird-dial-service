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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DialcodeV3Controller extends BaseController {
    
    private DialcodeManager dialCodeManager = new DialcodeManager();

    public CompletionStage<Result> generateDialCode(Http.Request request) {
        setCurrentRequest(request);
        String apiId = "sunbird.dialcode.generate";
        String channelId = request.header("X-Channel-ID").orElse(null);
        Request req = getRequest(request);
        try {
            Map<String, Object> map = (Map<String, Object>) req.get(DialCodeEnum.dialcodes.name());
            Response response = dialCodeManager.generateDialCode(map, channelId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while generating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public CompletionStage<Result> readDialCode(String dialCodeId) {
        String apiId = "sunbird.dialcode.read";
        try {
            Response response = dialCodeManager.readDialCode(dialCodeId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while reading Dial Code details : "+ e.getMessage(), e);
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
            Response response = dialCodeManager.updateDialCode(dialCodeId, channelId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
           TelemetryManager.error("Exception Occured while updating Dial Code : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public CompletionStage<Result> listDialCode(Http.Request request) {
        setCurrentRequest(request);
        String apiId = "sunbird.dialcode.list";
        Request req = getRequest(request);
        try {
            Map<String, Object> requestMap = (Map<String, Object>) req.get(DialCodeEnum.search.name());
            Response response = dialCodeManager.listDialCode(req.getContext(), requestMap);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while Performing List Operation for Dial Codes : "+ e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    public CompletionStage<Result> searchDialCode(Http.Request request) {
        setCurrentRequest(request);
        String apiId = "sunbird.dialcode.search";
        Request req = getRequest(request);
        try {
            Map<String, Object> map = (Map<String, Object>) req.get(DialCodeEnum.search.name());
            List<String> fieldsList = new ArrayList<String>();
            if(req.get(DialCodeEnum.fields.name()) != null) {
                try {
                    fieldsList = (List<String>) req.get(DialCodeEnum.fields.name());
                } catch (ClassCastException ce) {
                    fieldsList.add(req.get(DialCodeEnum.fields.name()).toString());
                }
                fieldsList.add("identifier");
            }
            Response response = dialCodeManager.searchDialCode(req.getContext(), map, fieldsList);
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
    public CompletionStage<Result> syncDialCode(Http.Request request) {
        setCurrentRequest(request);
        String apiId = "sunbird.dialcode.sync";
        String channelId = request.header("X-Channel-ID").orElse(null);
        String[] ids = request.queryString().get("identifier");
        List<String> identifiers = Optional.ofNullable(Arrays.asList(ids)).orElse(new ArrayList<>());
        Request req = getRequest(request);
        try {
            Map<String, Object> map = (Map<String, Object>) req.get("sync");
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
    public CompletionStage<Result> publishDialCode(Http.Request request, String dialCodeId) {
        setCurrentRequest(request);
        String apiId = "sunbird.dialcode.publish";
        String channelId = request.header("X-Channel-ID").orElse(null);
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

    public CompletionStage<Result> createPublisher(Http.Request request) {
        setCurrentRequest(request);

        String apiId = "sunbird.publisher.create";
        String channelId = request.header("X-Channel-ID").orElse(null);
        Request req = getRequest(request);
        try {
            Map<String, Object> map = (Map<String, Object>) req.get(DialCodeEnum.publisher.name());
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
    public CompletionStage<Result> readPublisher(String publisherId) {
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
    public CompletionStage<Result> updatePublisher(Http.Request request, String publisherId) {
        setCurrentRequest(request);
        String apiId = "sunbird.publisher.update";
        String channelId = request.header("X-Channel-ID").orElse(null);
        Request req = getRequest(request);
        try {
            Map<String, Object> map = (Map<String, Object>) req.get("publisher");
            Response response = dialCodeManager.updatePublisher(publisherId, channelId, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured while updating Publisher : " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }


}
