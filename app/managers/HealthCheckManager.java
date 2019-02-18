package managers;

import commons.dto.Response;
import telemetry.TelemetryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthCheckManager extends BaseManager implements IHealthCheckManager {
    public static boolean health = false;
    public static String CONNECTION_SUCCESS ="connection check is Successful";
    public static String CONNECTION_FAIL ="connection check has Failed";

    @Override
    public Response getAllServiceHealth(){
        List<Map<String, Object>> allChecks = new ArrayList<Map<String, Object>>();
        Map<String,Object> check = new HashMap<>();
        boolean status = false;
        boolean overAllHealth = true;
        try{
            status = IHealthCheckManager.checkElasticSearchHealth();
            if(!status){
                overAllHealth=false;
            }
            check = generateCheck("elasticsearch",check,status);
            TelemetryManager.log("elasticsearch "+CONNECTION_SUCCESS);
        }catch (Exception e){
            check = generateCheck("elasticsearch",check,status);
            TelemetryManager.error("elasticsearch "+CONNECTION_FAIL,e);
        }

        allChecks.add(check);
        check = new HashMap<>();
        try{
            status = IHealthCheckManager.checkRedisHealth();
            if(!status){
                overAllHealth=false;
            }
            check = generateCheck("redis cache",check,status);
            TelemetryManager.log("redis cache "+CONNECTION_SUCCESS);
        }catch (Exception e){
            check = generateCheck("redis cache",check,status);
            TelemetryManager.error("redis cache "+CONNECTION_FAIL,e);
        }

        allChecks.add(check);
        check = new HashMap<>();
        try{
            status = IHealthCheckManager.checkCassandraHealth();
            check = generateCheck("cassandra db",check,status);
            if(!status){
                overAllHealth=false;
            }
            TelemetryManager.log("cassandra db "+CONNECTION_SUCCESS);
        }catch (Exception e){
            check = generateCheck("cassandra db",check,status);
            TelemetryManager.error("cassandra db "+CONNECTION_FAIL,e);
        }
        allChecks.add(check);
        Response response = OK("checks",allChecks);
        response.put("healthy",overAllHealth);
        health = overAllHealth;
        return response;
    }

    protected Map<String,Object> generateCheck (String db, Map<String,Object> check,
                                                boolean status){
        if(status){
            check.put("name",db);
            check.put("healthy",status);
        }else{
            check.put("name",db);
            check.put("healthy",status);
            check.put("err","503");
            check.put("errmsg", db+" connection unavailable");
        }
        return check;
    }
}
