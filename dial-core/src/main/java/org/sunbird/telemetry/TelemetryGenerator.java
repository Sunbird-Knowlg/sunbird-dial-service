package org.sunbird.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.commons.AppConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link TelemetryGenerator} uses context and other parameters to generate
 * event JSON in string format.
 * 
 * @author Mahesh
 *
 */
public class TelemetryGenerator {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final String ENVIRONMENT = AppConfig.config.hasPath("telemetry_env")?AppConfig.config.getString("telemetry_env"):"dev";
	private static final String INSTALLATION_ID = AppConfig.config.hasPath("installation.id")?AppConfig.config.getString("installation.id"):"ekstep";
	private static final String DEFAULT_PRODUCER_ID = ENVIRONMENT + "." + INSTALLATION_ID + ".learning.platform";
	private static final String PRODUCER_VERSION = "1.0";
	private static String PRODUCER_PID = "";

	public static void setComponent(String component) {
		PRODUCER_PID = component;
	}

	/**
	 * To generate api_access LOG telemetry JSON string.
	 * 
	 * @param context
	 * @param params
	 * @return
	 */
	public static String access(Map<String, String> context, Map<String, Object> params) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("type", "api_access");
		edata.put("level", "INFO");
		edata.put("message", "");
		edata.put("params", getParamsList(params));
		Telemetry telemetry = new Telemetry("LOG", actor, eventContext, edata);
		return getTelemetry(telemetry);
	}

	/**
	 * To generate normal LOG telemetry JSON string with all params.
	 * 
	 * @param context
	 * @param type
	 * @param level
	 * @param message
	 * @param pageid
	 * @param params
	 * @return
	 */
	public static String log(Map<String, String> context, String type, String level, String message, String pageid,
			Map<String, Object> params) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("type", type);
		edata.put("level", level);
		edata.put("message", message);
		if (StringUtils.isNotBlank(pageid))
			edata.put("pageid", pageid);
		if (null != params && !params.isEmpty())
			edata.put("params", getParamsList(params));
		Telemetry telemetry = new Telemetry("LOG", actor, eventContext, edata);
		return getTelemetry(telemetry);
	}

	/**
	 * To generate normal LOG telemetry JSON string with required params.
	 * 
	 * @param context
	 * @param type
	 * @param level
	 * @param message
	 * @return
	 */
	public static String log(Map<String, String> context, String type, String level, String message) {
		return log(context, type, level, message, null, null);
	}

	/**
	 * To generate ERROR telemetry JSON string with all params.
	 * 
	 * @param context
	 * @param code
	 * @param type
	 * @param stacktrace
	 * @param pageid
	 * @param object
	 * @return
	 */
	public static String error(Map<String, String> context, String code, String type, String stacktrace, String pageid,
			Object object) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("err", code);
		edata.put("errtype", type);
		edata.put("stacktrace", stacktrace);
		if (StringUtils.isNotBlank(pageid))
			edata.put("pageid", pageid);
		if (null != object)
			edata.put("object", object);
		Telemetry telemetry = new Telemetry("ERROR", actor, eventContext, edata);
		return getTelemetry(telemetry);

	}

	/**
	 * To generate ERROR telemetry JSON string with required params.
	 * 
	 * @param context
	 * @param code
	 * @param type
	 * @param stacktrace
	 * @return
	 */
	public static String error(Map<String, String> context, String code, String type, String stacktrace) {
		return error(context, code, type, stacktrace, null, null);
	}

    /**
     * @param context
     * @param query
     * @param filters
     * @param sort
     * @param cData
     * @param size
     * @param topN
     * @param type
     * @return
     */
    public static String search(Map<String, String> context, String query, Object filters, Object sort,
                                List<Map<String, Object>> cData, int size, Object topN, String type) {
        Actor actor = getActor(context);
        Context eventContext = getContext(context);
        Map<String, Object> edata = new HashMap<String, Object>();
        edata.put("type", type);
        edata.put("query", query);
        edata.put("filters", filters);
        edata.put("sort", sort);
        edata.put("size", size);
        edata.put("topn", topN);
        Telemetry telemetry;
        if (null != cData) {
            telemetry = new Telemetry("SEARCH", actor, eventContext, edata, cData);
        } else {
            telemetry = new Telemetry("SEARCH", actor, eventContext, edata);
        }
        return getTelemetry(telemetry);
    }

	/**
	 * @param context
	 * @param props
	 * @param state
	 * @param prevState
	 * @param cdata
	 * @return
	 */
	public static String audit(Map<String, String> context, List<String> props, String state, String prevState,
			List<Map<String, Object>> cdata) {
		Telemetry telemetry = null;
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("props", props);
		if (StringUtils.isNotBlank(state))
			edata.put("state", state);
		if (StringUtils.isNotBlank(prevState))
			edata.put("prevstate", prevState);
		if (null != cdata && !cdata.isEmpty())
			telemetry = new Telemetry("AUDIT", actor, eventContext, edata, cdata);
		else
			telemetry = new Telemetry("AUDIT", actor, eventContext, edata);
		Target object = new Target(context.get("objectId"), context.get("objectType"));
		String pkgVersion = (String) context.get("pkgVersion");
		if (StringUtils.isNotBlank(pkgVersion))
			object.setVer(pkgVersion);
		telemetry.setObject(object);
		return getTelemetry(telemetry);
	}

	/**
	 * @param context
	 * @param props
	 * @param state
	 * @param prevState
	 * @return
	 */
	public static String audit(Map<String, String> context, List<String> props, String state, String prevState) {
		return audit(context, props, state, prevState, null);
	}

	private static Actor getActor(Map<String, String> context) {
		String actorId = context.get(TelemetryParams.ACTOR.name());
		if (StringUtils.isBlank(actorId))
			actorId = "org.ekstep.learning.platform";
		return new Actor(actorId, "System");

	}

	private static Context getContext(Map<String, String> context) {
		String channel = context.get(TelemetryParams.CHANNEL.name());
		String env = context.get(TelemetryParams.ENV.name());
		Context eventContext = new Context(channel, env, getProducer(context));
		String sid = context.get("sid");
		if (StringUtils.isNotBlank(sid))
			eventContext.setSid(sid);
		String did = context.get("did");
		if (StringUtils.isNotBlank(did))
			eventContext.setDid(did);

		return eventContext;
	}

	private static List<Map<String, Object>> getParamsList(Map<String, Object> params) {
		List<Map<String, Object>> paramsList = new ArrayList<Map<String, Object>>();
		if (null != params && !params.isEmpty()) {
			for (Entry<String, Object> entry : params.entrySet()) {
				Map<String, Object> param = new HashMap<String, Object>();
				param.put(entry.getKey(), entry.getValue());
				paramsList.add(param);
			}
		}
		return paramsList;
	}

	private static String getTelemetry(Telemetry telemetry) {
		String event = "";
		try {
			event = mapper.writeValueAsString(telemetry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}

	/**
	 * This Method Returns Producer Object for Telemetry Event.
	 * @param context
	 * @return Producer
	 */
	private static Producer getProducer(Map<String, String> context) {
		String appId = context.get(TelemetryParams.APP_ID.name());
		if (StringUtils.isNotBlank(appId))
			return new Producer(appId, PRODUCER_PID, PRODUCER_VERSION);
		else
			return new Producer(DEFAULT_PRODUCER_ID, PRODUCER_PID, PRODUCER_VERSION);
	}
}