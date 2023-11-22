/**
 *
 * @author Rhea Fernandes
 */
package org.sunbird.managers;

import com.datastax.driver.core.Session;
import org.sunbird.commons.JedisFactory;
import org.sunbird.commons.dto.Response;
import org.sunbird.elasticsearch.ElasticSearchUtil;
import org.elasticsearch.client.RestHighLevelClient;
import redis.clients.jedis.Jedis;
import org.sunbird.utils.CassandraConnector;
import org.sunbird.utils.Constants;

public interface IHealthCheckManager {

    Response getAllServiceHealth();

    static boolean checkRedisHealth(){
        try {
            Jedis jedis = JedisFactory.getRedisConncetion();
            jedis.close();
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

    static boolean checkElasticSearchHealth(){
        DialcodeManager dialcodeManager = new DialcodeManager();
        try{
            dialcodeManager.init();
            RestHighLevelClient restClient= ElasticSearchUtil.getClient(Constants.DIAL_CODE_INDEX);
            boolean response = restClient.ping();
            return response;
        }catch (Exception e){
//            e.printStackTrace();
            return false;
        }
    }

    static boolean checkCassandraHealth(){
        Session session ;
        try {
            session = CassandraConnector.getSession();
            if(null!= session && !session.isClosed()){
                session.execute("SELECT now() FROM system.local");
                return true;
            }else{
                return false;
            }
        }catch(Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

}
