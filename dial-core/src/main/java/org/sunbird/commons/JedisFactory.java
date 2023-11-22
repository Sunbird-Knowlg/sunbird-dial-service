package org.sunbird.commons;

import org.sunbird.commons.exception.ServerException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.sunbird.utils.DialCodeEnum;

public class JedisFactory {

	private static JedisPool jedisPool;

	private static int maxConnections = 128;
	private static String host = "localhost";
	private static int port = 6379;
	private static int index = 0;

	static {
		if (AppConfig.config.hasPath("redis.host")) host = AppConfig.config.getString("redis.host");
		if (AppConfig.config.hasPath("redis.port")) port = AppConfig.config.getInt("redis.port");
		if (AppConfig.config.hasPath("redis.maxConnections")) maxConnections = AppConfig.config.getInt("redis.maxConnections");
		if (AppConfig.config.hasPath("redis.dbIndex")) index = AppConfig.config.getInt("redis.dbIndex");
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxConnections);
		config.setBlockWhenExhausted(true);
		jedisPool = new JedisPool(config, host, port);
	}

	public static Jedis getRedisConncetion() {
		try {
			Jedis jedis = jedisPool.getResource();
			if (index > 0)
				jedis.select(index);
			return jedis;
		} catch (Exception e) {
			throw new ServerException(DialCodeEnum.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
		}
	}

	public static void returnConnection(Jedis jedis) {
		try {
			if (null != jedis)
				jedisPool.returnResource(jedis);
		} catch (Exception e) {
			throw new ServerException(DialCodeEnum.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
		}
	}
}