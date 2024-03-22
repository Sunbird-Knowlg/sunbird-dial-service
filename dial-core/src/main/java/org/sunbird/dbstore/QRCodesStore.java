/**
 * 
 */
package org.sunbird.dbstore;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.commons.AppConfig;
import org.sunbird.commons.DialCodeErrorCodes;
import org.sunbird.commons.DialCodeErrorMessage;
import org.sunbird.commons.exception.ResourceNotFoundException;
import org.sunbird.dto.QRCodesBatch;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.utils.DialCodeEnum;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author pradyumn
 *
 */
public class QRCodesStore extends CassandraStore {

	private static ObjectMapper mapper = new ObjectMapper();

	public QRCodesStore() {
		super();
		String keyspace = AppConfig.config.hasPath("qrcodes.keyspace.name")
				? AppConfig.config.getString("qrcodes.keyspace.name") : "dialcodes";
		String table = AppConfig.config.hasPath("qrcodes.keyspace.table")
				? AppConfig.config.getString("qrcodes.keyspace.table") : "dialcode_batch";
		String objectType = AppConfig.config.hasPath("qrcodes.object_type")
				? AppConfig.config.getString("qrcodes.object_type") : DialCodeEnum.QRCode.name();
		initialise(keyspace, table, objectType);
	}

	public QRCodesBatch read(String processId) throws Exception {
		QRCodesBatch qrCodesBatchObj = null;
		try {
			List<Row> rows = readByUUID(DialCodeEnum.processid.name(), processId);
			Row row = rows.get(0);
			qrCodesBatchObj = setQRCodesBatchData(row);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceNotFoundException(DialCodeErrorCodes.ERR_QRCODES_BATCH_INFO,
					DialCodeErrorMessage.ERR_QRCODES_BATCH_INFO + processId);
		}
		return qrCodesBatchObj;
	}

	private static QRCodesBatch setQRCodesBatchData(Row row) throws Exception {
		QRCodesBatch qrCodesBatchObj = new QRCodesBatch();
		try {
			qrCodesBatchObj.setProcessid(row.getUUID(DialCodeEnum.processid.name()));
			qrCodesBatchObj.setChannel(row.getString(DialCodeEnum.channel.name()));
			qrCodesBatchObj.setPublisher(row.getString(DialCodeEnum.publisher.name()));
			qrCodesBatchObj.setDialcodes(row.getList(DialCodeEnum.dialcodes.name(), String.class));
			qrCodesBatchObj.setStatus(row.getInt(DialCodeEnum.status.name()));
			qrCodesBatchObj.setCreatedOn(row.getTimestamp(DialCodeEnum.created_on.name()));
			String strURL = row.getString(DialCodeEnum.url.name());
			if (AppConfig.config.getBoolean("cloudstorage.metadata.replace_absolute_path")) {
				strURL = StringUtils.replace(strURL, AppConfig.config.getString("cloudstorage.relative_path_prefix"),
						AppConfig.config.getString("cloudstorage.read_base_path") + File.separator +
								AppConfig.config.getString("cloud_storage_container"));
			}
			qrCodesBatchObj.setUrl(strURL);
			Map<String, String> configMap = row.getMap(DialCodeEnum.config.name(), String.class, String.class);
			qrCodesBatchObj.setConfig(configMap);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return qrCodesBatchObj;
	}

}
