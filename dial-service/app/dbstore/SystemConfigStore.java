/**
 * 
 */
package dbstore;

import com.datastax.driver.core.Row;
import commons.AppConfig;
import utils.DialCodeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pradyumnaZ
 *
 */
public class SystemConfigStore extends CassandraStore {

	public SystemConfigStore() {
		super();
		String keyspace = AppConfig.config.hasPath("system.config.keyspace.name")
				? AppConfig.config.getString("system.config.keyspace.name")
				: "dialcode_store";
		String table = AppConfig.config.hasPath("system.config.table") ? AppConfig.config.getString("system.config.table")
				: "system_config";
		initialise(keyspace, table, "SystemConfig");
	}

	public Double getDialCodeIndex() throws Exception {
		List<Row> rows = read(DialCodeEnum.prop_key.name(), DialCodeEnum.dialcode_max_index.name());
		Row row = rows.get(0);
		return Double.valueOf(row.getString(DialCodeEnum.prop_value.name()));
	}

	public void setDialCodeIndex(double maxIndex) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.prop_value.name(), String.valueOf((int) maxIndex));
		update(DialCodeEnum.prop_key.name(), DialCodeEnum.dialcode_max_index.name(), data);
	}
}
