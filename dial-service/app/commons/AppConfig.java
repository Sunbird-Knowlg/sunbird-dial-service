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

}
