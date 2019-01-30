package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.DialCodeErrorCodes;
import commons.dto.Request;
import commons.dto.Response;
import commons.dto.ResponseParams;
import commons.exception.ClientException;
import commons.exception.MiddlewareException;
import commons.exception.ResourceNotFoundException;
import commons.exception.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BaseController extends Controller {
    private static ObjectMapper mapper = new ObjectMapper();

    protected Request getRequest() {
        JsonNode requestData = request().body().asJson();
        Request req = mapper.convertValue(requestData, Request.class);
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
        }
        return Results.internalServerError().status();
    }

}
