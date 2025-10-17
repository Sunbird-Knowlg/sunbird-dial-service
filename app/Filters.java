/**
 *
 * @author Rhea Fernandes
 */
import filters.HealthCheckFilter;
import play.mvc.EssentialFilter;
import play.http.HttpFilters;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class Filters implements HttpFilters {

    private HealthCheckFilter healthCheckFilter;

    @Inject
    public Filters(HealthCheckFilter healthCheckFilter) {
        this.healthCheckFilter = healthCheckFilter;
    }

    @Override
    public List<EssentialFilter> getFilters() {
        return Arrays.asList(healthCheckFilter.asJava());
    }
}