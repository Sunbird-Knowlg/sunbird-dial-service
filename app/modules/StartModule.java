package modules;

import com.google.inject.AbstractModule;
import managers.HealthCheckManager;
import telemetry.TelemetryGenerator;

public class StartModule extends AbstractModule {

    @Override
    protected void configure() {
        // Initialize system properties and telemetry
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        TelemetryGenerator.setComponent("dialcode-service");
        
        // Eager singleton to initialize health check on startup
        bind(HealthCheckInitializer.class).asEagerSingleton();
    }
    
    static class HealthCheckInitializer {
        @javax.inject.Inject
        public HealthCheckInitializer() {
            try {
                new HealthCheckManager().getAllServiceHealth();
            } catch (Exception e) {
                // Log but don't fail startup
                System.err.println("Health check initialization failed: " + e.getMessage());
            }
        }
    }
}
