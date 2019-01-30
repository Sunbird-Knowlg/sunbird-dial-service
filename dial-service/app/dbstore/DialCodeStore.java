package dbstore;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AppConfig;
import commons.DialCodeErrorCodes;
import commons.DialCodeErrorMessage;
import commons.exception.ResourceNotFoundException;
import dto.DialCode;
import org.apache.commons.lang3.StringUtils;
import utils.CassandraStoreParam;
import utils.DialCodeEnum;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Class is for all Dial Code CRUD Operation on Cassandra.
 * 
 * @author Pradyumna
 *
 */
public class DialCodeStore extends CassandraStore {

	private static ObjectMapper mapper = new ObjectMapper();

	public DialCodeStore() {
		super();
		String keyspace = AppConfig.config.hasPath("dialcode.keyspace.name")
				? AppConfig.config.getString("dialcode.keyspace.name") : "dialcode_store";
		String table = AppConfig.config.hasPath("dialcode.keyspace.table")
				? AppConfig.config.getString("dialcode.keyspace.table") : "dial_code";
		boolean index = AppConfig.config.hasPath("dialcode.index") ? AppConfig.config.getBoolean("dialcode.index") : true;
		String objectType = AppConfig.config.hasPath("dialcode.object_type")
				? AppConfig.config.getString("dialcode.object_type") : DialCodeEnum.DialCode.name();
		initialise(keyspace, table, objectType, index);
	}

	public void save(String channel, String publisher, String batchCode, String dialCode, Double dialCodeIndex)
			throws Exception {
		Map<String, Object> data = getInsertData(channel, publisher, batchCode, dialCode, dialCodeIndex);
		insert(dialCode, data);
	}

	public DialCode read(String dialCode) throws Exception {
		DialCode dialCodeObj = null;
		try {
			List<Row> rows = read(DialCodeEnum.identifier.name(), dialCode);
			Row row = rows.get(0);
			dialCodeObj = setDialCodeData(row);
		} catch (Exception e) {
			throw new ResourceNotFoundException(DialCodeErrorCodes.ERR_DIALCODE_INFO,
					DialCodeErrorMessage.ERR_DIALCODE_INFO + dialCode);
		}
		return dialCodeObj;
	}

	public void update(String id, Map<String, Object> data) throws Exception {
		update(DialCodeEnum.identifier.name(), id, data);
	}

	private static Map<String, Object> getInsertData(String channel, String publisher, String batchCode,
			String dialCode, Double dialCodeIndex) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.identifier.name(), dialCode);
		data.put(DialCodeEnum.channel.name(), channel);
		data.put(DialCodeEnum.publisher.name(), publisher);
		data.put(DialCodeEnum.batchcode.name(), batchCode);
		data.put(DialCodeEnum.dialcode_index.name(), dialCodeIndex);
		data.put(DialCodeEnum.status.name(), DialCodeEnum.Draft.name());
		data.put(DialCodeEnum.generated_on.name(), LocalDateTime.now().toString());
		return data;
	}

	private static DialCode setDialCodeData(Row row) throws Exception {
		DialCode dialCodeObj = new DialCode();
		dialCodeObj.setIdentifier(row.getString(DialCodeEnum.identifier.name()));
		dialCodeObj.setChannel(row.getString(DialCodeEnum.channel.name()));
		dialCodeObj.setPublisher(row.getString(DialCodeEnum.publisher.name()));
		dialCodeObj.setBatchCode(row.getString(DialCodeEnum.batchCode.name()));
		dialCodeObj.setStatus(row.getString(DialCodeEnum.status.name()));
		dialCodeObj.setGeneratedOn(row.getString(DialCodeEnum.generated_on.name()));
		dialCodeObj.setPublishedOn(row.getString(DialCodeEnum.published_on.name()));
		String metadata = row.getString(DialCodeEnum.metadata.name());
		Map<String, Object> metaData = null;
		if (!StringUtils.isBlank(metadata)) {
			metaData = mapper.readValue(metadata, new TypeReference<Map<String, Object>>() {
			});
		}
		dialCodeObj.setMetadata(metaData);
		return dialCodeObj;
	}

	public int sync(Map<String, Object> map) {
		List<Row> rows = getRecordsByProperties(map);
		Map<String, Object> syncRequest = new HashMap<String, Object>();
		for (Row row : rows) {
			syncRequest = new HashMap<String, Object>();
			syncRequest.put(DialCodeEnum.identifier.name(), row.getString(DialCodeEnum.identifier.name()));
			syncRequest.put(DialCodeEnum.channel.name(), row.getString(DialCodeEnum.channel.name()));
			syncRequest.put(DialCodeEnum.publisher.name(), row.getString(DialCodeEnum.publisher.name()));
			syncRequest.put(DialCodeEnum.batchcode.name(), row.getString(DialCodeEnum.batchCode.name()));
			syncRequest.put(DialCodeEnum.status.name(), row.getString(DialCodeEnum.status.name()));
			syncRequest.put(DialCodeEnum.metadata.name(), row.getString(DialCodeEnum.metadata.name()));
			syncRequest.put(DialCodeEnum.generated_on.name(), row.getString(DialCodeEnum.generated_on.name()));
			syncRequest.put(DialCodeEnum.published_on.name(), row.getString(DialCodeEnum.published_on.name()));
			logTransactionEvent(CassandraStoreParam.UPDATE.name(), row.getString(DialCodeEnum.identifier.name()),
					syncRequest);
		}

		return rows.size();

	}

}
