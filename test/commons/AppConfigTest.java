package commons;

import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AppConfigTest {

	@BeforeClass
	public static void init() {
		AppConfig.config = ConfigFactory.parseMap(new HashMap<String, Object>() {{
			put("test.str", "strval");
			put("test.int", 100);
			put("test.bool", true);
			put("test.long", 1380914990);
			put("test.double", 900923.0);
			put("content.graph_ids", Arrays.asList("es","ko"));
			put("test.strlist", new ArrayList<String>() {{
				add("val1");
				add("val2");
			}});
			put("test.map", new HashMap<String, Object>() {{
				put("key1", "val1");
				put("key2","val2");
			}});
		}}).resolve();
	}

	@Test
	public void testGetStringWithValidConfig() {
		String str = AppConfig.getString("test.str", "def_str_val");
		Assert.assertEquals("strval", str);
	}

	@Test
	public void testGetStringWithInvalidConfig() {
		String str = AppConfig.getString("test.str.1", "def_str_val");
		Assert.assertEquals("def_str_val", str);
	}

	@Test
	public void testGetDoubleWithValidConfig() {
		Double result = AppConfig.getDouble("test.long", 0.0);
		Double expected = 1.38091499E9;
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
		boolean result = AppConfig.getBoolean("test.bool", false);
		Assert.assertEquals(true, result);
	}

	@Test
	public void testGetBooleanWithInvalidConfig() {
		boolean result = AppConfig.getBoolean("test.bool.1", false);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testGetLongWithValidConfig() {
		Long result = AppConfig.getLong("test.long", 0L);
		Long expected = 1380914990L;
		Assert.assertTrue(null != result );
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testGetLongWithInvalidConfig() {
		Long result = AppConfig.getLong("test.long.1", 0L);
		Long expected = 0L;
		Assert.assertTrue(null != result );
		Assert.assertEquals(expected, result);
	}

}
