package org.sunbird.managers;

import org.sunbird.commons.dto.Property;
import org.sunbird.commons.dto.Request;
import org.sunbird.commons.dto.Response;
import org.sunbird.commons.dto.ResponseParams;
import org.sunbird.commons.exception.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.telemetry.TelemetryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class BaseManager {

	protected Request setContext(Request request, String graphId, String manager, String operation) {
		request.getContext().put("graph_id", graphId);
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	protected Request getRequest(String graphId, String manager, String operation) {
		Request request = new Request();
		return setContext(request, graphId, manager, operation);
	}

	protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	protected Response ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier,
			Object vo) {
		Response response = new Response();
		response.put(responseIdentifier, vo);
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(code);
		return response;
	}

	protected Response OK() {
		Response response = new Response();
		response.setParams(getSucessStatus());
		return response;
	}

	protected Response OK(String responseIdentifier, Object vo) {
		Response response = new Response();
		response.put(responseIdentifier, vo);
		response.setParams(getSucessStatus());
		return response;
	}

	protected boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(ResponseParams.StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}

	protected String getErrorMessage(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			return params.getErrmsg();
		}
		return null;
	}

	protected Response copyResponse(Response res) {
		Response response = new Response();
		response.setResponseCode(res.getResponseCode());
		response.setParams(res.getParams());
		return response;
	}

	protected Response copyResponse(Response to, Response from) {
		to.setResponseCode(from.getResponseCode());
		to.setParams(from.getParams());
		return to;
	}

	protected ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(ResponseParams.StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	protected ResponseParams getSucessStatus() {
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(ResponseParams.StatusType.successful.name());
		params.setErrmsg("Operation successful");
		return params;
	}

	protected boolean validateRequired(Object... objects) {
		boolean valid = true;
		for (Object baseValueObject : objects) {
			if (null == baseValueObject) {
				valid = false;
				break;
			}
			if (baseValueObject instanceof String) {
				if (StringUtils.isBlank(((String) baseValueObject))) {
					valid = false;
					break;
				}
			}
			if (baseValueObject instanceof List<?>) {
				List<?> list = (List<?>) baseValueObject;
				if (null == list || list.isEmpty()) {
					valid = false;
					break;
				}
			}
			if (baseValueObject instanceof Map<?, ?>) {
				Map<?, ?> map = (Map<?, ?>) baseValueObject;
				if (null == map || map.isEmpty()) {
					valid = false;
					break;
				}
			}
			if (baseValueObject instanceof Property) {
				Property property = (Property) baseValueObject;
				if (StringUtils.isBlank(property.getPropertyName())
						|| (null == property.getPropertyValue() && null == property.getDateValue())) {
					valid = false;
					break;
				}
			}
		}
		return valid;
	}

	protected Response getSuccessResponse() {
		Response resp = new Response();
		ResponseParams respParam = new ResponseParams();
		respParam.setStatus("successful");
		resp.setParams(respParam);
		return resp;
	}

	protected Response getPartialSuccessResponse() {
		Response resp = new Response();
		ResponseParams respParam = new ResponseParams();
		respParam.setStatus("partial successful");
		resp.setResponseCode(ResponseCode.PARTIAL_SUCCESS);
		resp.setParams(respParam);
		return resp;
	}

}
