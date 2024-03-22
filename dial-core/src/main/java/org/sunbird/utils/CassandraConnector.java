package org.sunbird.utils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import org.sunbird.commons.AppConfig;
import org.sunbird.commons.exception.ServerException;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.telemetry.TelemetryManager;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CassandraConnector {

	/** Cassandra Session Map. */
	private static Map<String,Session> sessionMap=new HashMap<>();

    static {
        prepareSession("lp", getConsistencyLevel("lp"));
    }

	/**
	 * Provide lp Session.
	 *
	 * @return lp session.
	 */
	public static Session getSession() {
		return getSession("lp");
	}

	/**
	 * @param sessionKey
	 * @return
	 */
    public static Session getSession(String sessionKey) {
        Session session = sessionMap.containsKey(sessionKey) ? sessionMap.get(sessionKey) : null;

        if (null == session || session.isClosed()) {
			ConsistencyLevel level = getConsistencyLevel(sessionKey);
			prepareSession(sessionKey, level);
            session = sessionMap.get(sessionKey);
        }
        if (null == session)
            throw new ServerException("ERR_INITIALISE_CASSANDRA_SESSION", "Error while initialising cassandra");
        return session;
    }

	/**
	 *This Method Create a session with Cassandra Cluster and Stores it into Local Session Cache against session key.
	 * @param sessionKey
	 * @param level
	 */
	private static void prepareSession(String sessionKey, ConsistencyLevel level) {
		List<String> connectionInfo = getConnectionInfo(sessionKey.toLowerCase());
		List<InetSocketAddress> addressList = getSocketAddress(connectionInfo);
		try {
			if (null != level) {
				sessionMap.put(sessionKey.toLowerCase(), Cluster.builder().addContactPointsWithPorts(addressList).withQueryOptions(new QueryOptions().setConsistencyLevel(level)).build().connect());
			} else {
				sessionMap.put(sessionKey.toLowerCase(), Cluster.builder().addContactPointsWithPorts(addressList).build().connect());
			}

			registerShutdownHook();
		} catch (Exception e) {
			TelemetryManager.error("Error! While Loading Cassandra Properties." + e.getMessage(), e);
		}
	}

	/**
	 *
	 * @param sessionKey
	 * @return
	 */
	private static List<String> getConnectionInfo(String sessionKey) {
		List<String> connectionInfo = null;
		if (sessionKey.equalsIgnoreCase("lp")) {
			connectionInfo = Arrays.asList(AppConfig.config.getString("cassandra.lp.connection").split(","));
		} else if (sessionKey.equalsIgnoreCase("lpa")) {
			connectionInfo = Arrays.asList(AppConfig.config.getString("cassandra.lpa.connection").split(","));
		} else if (sessionKey.equalsIgnoreCase("sunbird")) {
			connectionInfo = Arrays.asList(AppConfig.config.getString("cassandra.sunbird.connection").split(","));
		}
		if (null == connectionInfo || connectionInfo.isEmpty())
			connectionInfo = new ArrayList<>(Arrays.asList("localhost:9042"));

		return connectionInfo;
	}

	/**
	 *
	 * @param hosts
	 * @return
	 */
	private static List<InetSocketAddress> getSocketAddress(List<String> hosts) {
		List<InetSocketAddress> connectionList = new ArrayList<>();
		for (String connection : hosts) {
			String[] conn = connection.split(":");
			String host = conn[0];
			int port = Integer.valueOf(conn[1]);
			connectionList.add(new InetSocketAddress(host, port));
		}
		return connectionList;
	}


	/**
	 * Close connection with the cluster.
	 *
	 */
    public static void close() {
        sessionMap.entrySet().stream().forEach(stream -> stream.getValue().close());
    }

	/**
	 * Register JVM shutdown hook to close cassandra open session.
	 */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //TelemetryManager.log("Shutting down Cassandra connector session");
                CassandraConnector.close();
            }
        });
    }

	/**
	 * This Method Returns the value of Consistency Level for Multi Node/DC Cassandra Cluster.
	 * @param clusterName
	 * @return
	 */
	private static ConsistencyLevel getConsistencyLevel(String clusterName) {
		String key = "cassandra." + clusterName + ".consistency.level";
		String consistencyLevel = AppConfig.config.hasPath(key) ?
				AppConfig.config.getString(key) : null;
		if (StringUtils.isNotBlank(consistencyLevel))
			return ConsistencyLevel.valueOf(consistencyLevel.toUpperCase());
		return null;
	}

}
