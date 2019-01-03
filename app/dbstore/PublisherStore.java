/**
 * 
 */
package dbstore;

import com.datastax.driver.core.Row;
import commons.AppConfig;

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
		String keyspace = "dialcode_store";
		if (AppConfig.config.hasPath("publisher.keyspace.name"))
			keyspace = AppConfig.config.getString("publisher.keyspace.name");
		initialise(keyspace, "publisher", "Publisher");
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
		update(key, idValue, props);
		List<String> keys = props.keySet().stream().collect(Collectors.toList());
		//TelemetryManager.audit(idValue, getObjectType(), keys, null, null);
	}
}
