package commons;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Pradyumna
 *
 */

public class AppConfig {
	private static Config defaultConf = ConfigFactory.load();
	private static Config envConf = ConfigFactory.systemEnvironment();
	public static Config config = defaultConf.withFallback(envConf);

	private static int requestTimeout = 30;
	private static Map<String, List<String>> graphIds = new HashMap<>();

	
	public static void loadProperties(Config conf) {
		config = config.withFallback(conf);
	}

	public static int getTimeout() {
		return requestTimeout;
	}

	public static String getString(String key, String defaultValue) {
        return config.hasPath(key) ? config.getString(key) : defaultValue;
    }

    public static Double getDouble(String key, Double defaultValue) {
        return config.hasPath(key) ? config.getDouble(key) : defaultValue;
    }

    public static Long getLong(String key, Long defaultValue) {
        return config.hasPath(key) ? config.getLong(key) : defaultValue;
    }

	public static Boolean getBoolean(String key, Boolean defaultValue) {
		return config.hasPath(key) ? config.getBoolean(key) : defaultValue;
	}

}
