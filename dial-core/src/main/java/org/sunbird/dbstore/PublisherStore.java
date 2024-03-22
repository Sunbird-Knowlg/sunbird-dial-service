/**
 * 
 */
package org.sunbird.dbstore;

import com.datastax.driver.core.Row;
import org.sunbird.commons.AppConfig;
import org.sunbird.utils.DialCodeEnum;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pradyumn
 *
 */
public class PublisherStore extends CassandraStore {

	public PublisherStore() {
		super();
		String keyspace = AppConfig.config.hasPath("publisher.keyspace.name")
				? AppConfig.config.getString("publisher.keyspace.name") : "dialcode_store";
		String table = AppConfig.config.hasPath("publisher.keyspace.table")
				? AppConfig.config.getString("publisher.keyspace.table") : "publisher";
		String objectType = AppConfig.config.hasPath("publisher.object_type")
				? AppConfig.config.getString("publisher.object_type") : DialCodeEnum.Publisher.name();
		initialise(keyspace, table, objectType);
	}


	public void create(String id, Map<String, Object> props) {
		insert(id, props);
		List<String> keys = props.keySet().stream().collect(Collectors.toList());
		keys.add("identifier");
		//TelemetryManager.audit(id, getObjectType(), keys, null, null);
	}
	
	public List<Row> get(String id, Object value) {
		return read(id, value);
	}
	
	public void modify(String key, String idValue, Map<String, Object> props) {
		update(key, idValue, props, null);
		List<String> keys = props.keySet().stream().collect(Collectors.toList());
		//TelemetryManager.audit(idValue, getObjectType(), keys, null, null);
	}
}
