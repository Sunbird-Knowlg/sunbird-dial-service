package manager;

import commons.dto.Response;
import managers.HealthCheckManager;
import managers.IHealthCheckManager;
import common.CassandraTestSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HealthCheckManagerTest extends CassandraTestSetup{

    HealthCheckManager healthCheckManager;
    boolean goodHealth;
    @Before
    public  void setup() throws Exception{
        healthCheckManager = new HealthCheckManager();
        goodHealth = true;
    }

    @After
    public  void teardown()throws Exception{
        healthCheckManager = null;
    }

    @Ignore("requires live OpenSearch instance on localhost:9200")
    @Test
    public void getAllServicesHealthTest() throws Exception{
        String responseCode ="OK";
        Response actualHealth = healthCheckManager.getAllServiceHealth();
        assertSame(goodHealth,Boolean.parseBoolean(actualHealth.getResult().get("healthy").toString()));
        assertEquals(responseCode,actualHealth.getResponseCode().name());

    }

    /**
     * When Redis is disabled (default in tests), the health response must contain
     * elasticsearch and cassandra db checks but must NOT contain redis cache or
     * DIAL Max Index checks.
     */
    @Test
    public void getAllServicesHealthWithRedisDisabledTest() throws Exception {
        // redis.cache.enabled defaults to false in the test config
        Response response = healthCheckManager.getAllServiceHealth();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) response.getResult().get("checks");
        assertNotNull("checks list must be present in health response", checks);

        boolean hasEs = false, hasCassandra = false, hasRedis = false, hasDialMaxIndex = false;
        for (Map<String, Object> check : checks) {
            String name = (String) check.get("name");
            if ("elasticsearch".equals(name))   hasEs = true;
            if ("cassandra db".equals(name))    hasCassandra = true;
            if ("redis cache".equals(name))     hasRedis = true;
            if ("DIAL Max Index".equals(name))  hasDialMaxIndex = true;
        }

        assertTrue("elasticsearch check must be present", hasEs);
        assertTrue("cassandra db check must be present", hasCassandra);
        assertFalse("redis cache check must not be present when Redis is disabled", hasRedis);
        assertFalse("DIAL Max Index check must not be present when Redis is disabled", hasDialMaxIndex);
    }

    @Test
    public void checkRedisHealth() throws Exception{
        boolean actualHealth = IHealthCheckManager.checkRedisHealth();
        assertEquals(goodHealth,actualHealth);
    }

    @Test
    public void checkCassandraHealth() throws Exception{
        boolean actualHealth = IHealthCheckManager.checkCassandraHealth();
        assertEquals(goodHealth,actualHealth);
    }

    @Ignore("requires live OpenSearch instance on localhost:9200")
    @Test
    public void checkElasticSearchHealth() throws Exception{
        boolean actualHealth = IHealthCheckManager.checkElasticSearchHealth();
        assertEquals(goodHealth,actualHealth);
    }


}
