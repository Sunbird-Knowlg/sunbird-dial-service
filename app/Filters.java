import filters.HealthCheckFilter;
import play.api.mvc.EssentialFilter;
import play.http.HttpFilters;
import javax.inject.Inject;

public class Filters implements HttpFilters {

    private HealthCheckFilter healthCheckFilter;

    @Inject
    public Filters(HealthCheckFilter healthCheckFilter) {
        this.healthCheckFilter= healthCheckFilter;
    }

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[] {(EssentialFilter) this.healthCheckFilter };
    }
}