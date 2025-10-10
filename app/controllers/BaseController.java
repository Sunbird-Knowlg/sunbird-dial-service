package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AppConfig;
import commons.DialCodeErrorCodes;
import commons.dto.HeaderParam;
import commons.dto.Request;
import commons.dto.Response;
import commons.dto.ResponseParams;
import commons.exception.*;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import telemetry.TelemetryParams;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

public class BaseController extends Controller {
    private static ObjectMapper mapper = new ObjectMapper();

    protected Request getRequest(Http.Request httpRequest) {
        JsonNode requestData = httpRequest.body().asJson();
        Request req = mapper.convertValue(requestData, Request.class);
        setHeaderContext(httpRequest,req);
        return req;
    }
    
    // Keep old method for backward compatibility but mark deprecated
    @Deprecated
    protected Request getRequest() {
        return getRequest(request());
    }


    protected CompletionStage<Result> getResponseEntity(Response response, String apiId, String msgId) {
        int statusCode = response.getResponseCode().code();
        setResponseEnvelope(response, apiId, msgId);
        return CompletableFuture.completedFuture(Results.status(statusCode ,Json.toJson(response)).as("application/json"));
    }

    protected CompletionStage<Result> getExceptionResponseEntity(Exception e, String apiId, String msgId) {
        int statusCode = getStatus(e);
        Response response = getErrorResponse(e);
        setResponseEnvelope(response, apiId, msgId);
        return CompletableFuture.completedFuture(Results.status(statusCode ,Json.toJson(response)).as("application/json"));
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
