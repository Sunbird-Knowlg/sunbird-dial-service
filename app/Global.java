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

public class Global {

    private static final ALogger accessLogger = Logger.of("accesslog");
    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    public Global() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        TelemetryGenerator.setComponent("dialcode-service");
        new HealthCheckManager().getAllServiceHealth();
    }
}
