package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.commons.AppConfig;
import org.sunbird.commons.DialCodeErrorCodes;
import org.sunbird.commons.dto.HeaderParam;
import org.sunbird.commons.dto.Request;
import org.sunbird.commons.dto.Response;
import org.sunbird.commons.dto.ResponseParams;
import org.sunbird.commons.exception.*;
import org.apache.commons.lang3.StringUtils;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import org.sunbird.telemetry.TelemetryParams;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BaseController extends Controller {
    private static ObjectMapper mapper = new ObjectMapper();

    protected Request getRequest() {
        JsonNode requestData = request().body().asJson();
        Request req = mapper.convertValue(requestData, Request.class);
        setHeaderContext(request(),req);
        return req;
    }


    protected Promise<Result> getResponseEntity(Response response, String apiId, String msgId) {
        int statusCode = response.getResponseCode().code();
        setResponseEnvelope(response, apiId, msgId);
        return Promise.<Result>pure(Results.status(statusCode ,Json.toJson(response)).as("application/json"));
    }

    protected Promise<Result> getExceptionResponseEntity(Exception e, String apiId, String msgId) {
        int statusCode = getStatus(e);
        Response response = getErrorResponse(e);
        setResponseEnvelope(response, apiId, msgId);
        return Promise.<Result>pure(Results.status(statusCode ,Json.toJson(response)).as("application/json"));
    }

    public Result getServiceUnavailableResponseEntity(Exception e, String apiId, String msgId) {
        int statusCode = getStatus(e);
        Response response = getErrorResponse(e);
        setResponseEnvelope(response, apiId, msgId);
        return Results.status(statusCode ,Json.toJson(response)).as("application/json");
    }


    private void setResponseEnvelope(Response response, String apiId, String msgId) {
        if (null != response) {
            response.setId(apiId);
            response.setVer(getAPIVersion());
            response.setTs(getResponseTimestamp());
            ResponseParams params = response.getParams();
            if (null == params)
                params = new ResponseParams();
            if (StringUtils.isNotBlank(msgId))
                params.setMsgid(msgId);
            params.setResmsgid(getUUID());
            if (StringUtils.equalsIgnoreCase(ResponseParams.StatusType.successful.name(), params.getStatus())) {
                params.setErr(null);
                params.setErrmsg(null);
            }
            response.setParams(params);
        }
    }

    protected String getAPIVersion() {
        return "3.0";
    }

    private String getResponseTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
        return sdf.format(new Date());
    }

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }

    private Response getErrorResponse(Exception e) {
        Response response = new Response();
        ResponseParams resStatus = new ResponseParams();
        String message = e.getMessage();
        resStatus.setErrmsg(message);
        resStatus.setStatus(ResponseParams.StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException me = (MiddlewareException) e;
            resStatus.setErr(me.getErrCode());
            response.setResponseCode(me.getResponseCode());
        } else {
            resStatus.setErr(DialCodeErrorCodes.SYSTEM_ERROR);
            response.setResponseCode(ResponseCode.SERVER_ERROR);
        }
        response.setParams(resStatus);
        return response;
    }

    private int getStatus(Exception e) {
        if (e instanceof ClientException) {
            return Results.badRequest().status();
        } else if (e instanceof ResourceNotFoundException) {
            return Results.notFound().status();
        }else if (e instanceof ServiceUnavailableException){
            return Results.status(ResponseCode.SERVICE_UNAVAILABLE.code()).status();
        }
        return Results.internalServerError().status();
    }

    /**
     *
     * @param httpRequest
     * @param dialRequest
     */
    protected void setHeaderContext(Http.Request httpRequest, Request dialRequest) {
        String sessionId = httpRequest.getHeader("X-Session-ID");
        String consumerId = httpRequest.getHeader("X-Consumer-ID");
        String deviceId = httpRequest.getHeader("X-Device-ID");
        String authUserId = httpRequest.getHeader("X-Authenticated-Userid");
        String channelId = httpRequest.getHeader("X-Channel-ID");
        String appId = httpRequest.getHeader("X-App-Id");

        if (StringUtils.isNotBlank(sessionId))
            dialRequest.getContext().put("SESSION_ID", sessionId);
        if (StringUtils.isNotBlank(consumerId))
            dialRequest.getContext().put(HeaderParam.CONSUMER_ID.name(), consumerId);
        if (StringUtils.isNotBlank(deviceId))
            dialRequest.getContext().put(HeaderParam.DEVICE_ID.name(), deviceId);
        if (StringUtils.isNotBlank(authUserId))
            dialRequest.getContext().put(HeaderParam.CONSUMER_ID.name(), authUserId);
        if (StringUtils.isNotBlank(channelId))
            dialRequest.getContext().put(HeaderParam.CHANNEL_ID.name(), channelId);
        else
            dialRequest.getContext().put(HeaderParam.CHANNEL_ID.name(), AppConfig.config.getString("channel.default"));
        if (StringUtils.isNotBlank(appId))
            dialRequest.getContext().put(HeaderParam.APP_ID.name(), appId);

        dialRequest.getContext().put(TelemetryParams.ENV.name(), "dialcode");

        if (null != dialRequest.getContext().get(HeaderParam.CONSUMER_ID.name())) {
            dialRequest.put(TelemetryParams.ACTOR.name(), dialRequest.getContext().get(HeaderParam.CONSUMER_ID.name()));
        } else if (null != dialRequest && null != dialRequest.getParams().getCid()) {
            dialRequest.put(TelemetryParams.ACTOR.name(), dialRequest.getParams().getCid());
        } else {
            dialRequest.put(TelemetryParams.ACTOR.name(), "learning.platform");
        }

    }

}
