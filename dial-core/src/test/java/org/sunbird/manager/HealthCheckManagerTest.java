package org.sunbird.manager;

import org.sunbird.commons.dto.Response;
import org.sunbird.managers.HealthCheckManager;
import org.sunbird.managers.IHealthCheckManager;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HealthCheckManagerTest {

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

    @Test
    public void getAllServicesHealthTest() throws Exception{
        String responseCode ="OK";
        Response actualHealth = healthCheckManager.getAllServiceHealth();
        assertSame(goodHealth,Boolean.parseBoolean(actualHealth.getResult().get("healthy").toString()));
        assertEquals(responseCode,actualHealth.getResponseCode().name());

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

    @Test
    public void checkElasticSearchHealth() throws Exception{
        boolean actualHealth = IHealthCheckManager.checkElasticSearchHealth();
        assertEquals(goodHealth,actualHealth);
    }


}
