package managers;

import commons.dto.Response;
import telemetry.TelemetryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthCheckManager extends BaseManager implements IHealthCheckManager {
    public static String CONNECTION_SUCCESS ="Connection Check is Successful";
    public static String CONNECTION_FAIL ="Connection Check has Failed";

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
            check = generateCheck("Search-db",check,status);
            TelemetryManager.log("Elastic-Search "+CONNECTION_SUCCESS);
        }catch (Exception e){
            check = generateCheck("Search-db",check,status);
            TelemetryManager.error("Elastic-Search "+CONNECTION_FAIL,e);
        }

        allChecks.add(check);
        check = new HashMap<>();
        try{
            status = IHealthCheckManager.checkRedisHealth();
            if(!status){
                overAllHealth=false;
            }
            check = generateCheck("Redis-Cache",check,status);
            TelemetryManager.log("Redis-Cache "+CONNECTION_SUCCESS);
        }catch (Exception e){
            check = generateCheck("Redis-Cache",check,status);
            TelemetryManager.error("Redis-Cache "+CONNECTION_FAIL,e);
        }

        allChecks.add(check);
        check = new HashMap<>();
        try{
            status = IHealthCheckManager.checkCassandraHealth();
            check = generateCheck("Cassandra",check,status);
            if(!status){
                overAllHealth=false;
            }
            TelemetryManager.log("Cassandra "+CONNECTION_SUCCESS);
        }catch (Exception e){
            check = generateCheck("Cassandra",check,status);
            TelemetryManager.error("Cassandra "+CONNECTION_FAIL,e);
        }
        allChecks.add(check);
        Response response = OK("checks",allChecks);
        response.put("healthy",overAllHealth);
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
            check.put("errmsg", db+" Connection Unavailable");
        }
        return check;
    }
}
