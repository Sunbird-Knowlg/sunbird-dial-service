import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AppConfig;
import commons.dto.ExecutionContext;
import commons.dto.HeaderParam;
import commons.dto.Response;
import managers.HealthCheckManager;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Logger.ALogger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import telemetry.TelemetryAccessEventUtil;
import telemetry.TelemetryGenerator;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class Global extends play.http.DefaultHttpRequestHandler {

    private static final ALogger accessLogger = Logger.of("accesslog");
    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    public Global() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        TelemetryGenerator.setComponent("dialcode-service");
        new HealthCheckManager().getAllServiceHealth();
    }

    @SuppressWarnings("rawtypes")
    public Action<?> wrapAction(Action<?> action) {
        return new Action.Simple() {
            public CompletionStage<Result> call(Http.Request request) {
                long startTime = System.currentTimeMillis();
                CompletionStage<Result> call = action.call(request);
                call.thenAccept((r) -> {
                    try {
                        String path = request.uri();
                        if (!path.contains("/health")) {
                            JsonNode requestData = request.body().asJson();
                            commons.dto.Request req = mapper.convertValue(requestData,
                                    commons.dto.Request.class);

                            // Get response body as bytes
                            byte[] body = r.body().consumeData(play.core.j.JavaHelpers.materializer()).toCompletableFuture().get().toArray();
                            Response responseObj = mapper.readValue(body, Response.class);

                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put("StartTime", startTime);
                            data.put("Request", req);
                            data.put("Response", responseObj);
                            data.put("RemoteAddress", request.remoteAddress());
                            data.put("ContentLength", body.length);
                            data.put("Status", r.status());
                            data.put("Protocol", request.secure() ? "HTTPS" : "HTTP");
                            data.put("Method", request.method());
                            data.put("X-Session-ID", request.header("X-Session-ID").orElse(null));
                            String consumerId = request.header("X-Consumer-ID").orElse(null);
                            data.put("X-Consumer-ID", consumerId);
                            String deviceId = request.header("X-Device-ID").orElse(null);
                            data.put("X-Device-ID", deviceId);
                            if(StringUtils.isNotBlank(deviceId))
                                ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.DEVICE_ID.name(),
                                        deviceId);
                            data.put("X-Authenticated-Userid", request.header("X-Authenticated-Userid").orElse(null));
                            if (StringUtils.isNotBlank(consumerId))
                                ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CONSUMER_ID.name(),
                                        consumerId);
                            data.put("env", "dialcode");
                            data.put("path", path);
                            String channelId = request.header("X-Channel-ID").orElse(null);
                            if (StringUtils.isNotBlank(channelId))
                                ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(),
                                        channelId);
                            else
                                ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(),
                                        AppConfig.config.getString("channel.default"));

                            String appId = request.header("X-App-ID").orElse(null);
                            if(StringUtils.isNotBlank(appId)){
                                ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.APP_ID.name(),
                                        channelId);
                            }
                            data.put(HeaderParam.APP_ID.name(), appId);
                            TelemetryAccessEventUtil.writeTelemetryEventLog(data);
                            accessLogger.info(request.remoteAddress() + " " + request.host() + " " + request.method()
                                    + " " + request.uri() + " " + r.status() + " " + body.length);
                        }
                    } catch (Exception e) {
                        accessLogger.error(e.getMessage());
                    }
                });
                return call;
            }
        };
    }
}
