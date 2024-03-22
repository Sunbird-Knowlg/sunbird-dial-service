package org.sunbird.commons;

import org.junit.Assert;
import org.junit.Test;

public class AppConfigTest {

	@Test
	public void testGetStringWithValidConfig() {
		String str = AppConfig.getString("system.config.keyspace.name", "def_str_val");
		Assert.assertEquals("dialcode_store_test", str);
	}

	@Test
	public void testGetStringWithInvalidConfig() {
		String str = AppConfig.getString("test.str.1", "def_str_val");
		Assert.assertEquals("def_str_val", str);
	}

	@Test
	public void testGetDoubleWithValidConfig() {
		Double result = AppConfig.getDouble("dialcode.length", 0.0);
		Double expected = 6.0;
		Assert.assertTrue(null != result );
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testGetDoubleWithInvalidConfig() {
		Double result = AppConfig.getDouble("test.long.1", 0.0);
		Double expected = 0.0;
		Assert.assertTrue(null != result );
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testGetBooleanWithValidConfig() {
		boolean result = AppConfig.getBoolean("kafka.topic.send_enable", true);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testGetBooleanWithInvalidConfig() {
		boolean result = AppConfig.getBoolean("test.bool", false);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testGetLongWithInvalidConfig() {
		Long result = AppConfig.getLong("test.long.1", 0L);
		Long expected = 0L;
		Assert.assertTrue(null != result );
		Assert.assertEquals(expected, result);
	}

}
