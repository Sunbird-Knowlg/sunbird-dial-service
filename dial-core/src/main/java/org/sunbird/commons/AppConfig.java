package org.sunbird.commons;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 
 * @author Pradyumna
 *
 */

public class AppConfig {
	private static Config defaultConf = ConfigFactory.load();
	private static Config envConf = ConfigFactory.systemEnvironment();
	public static Config config = defaultConf.withFallback(envConf);

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
