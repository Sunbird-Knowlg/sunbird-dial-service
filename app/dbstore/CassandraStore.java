/**
 *
 */
package dbstore;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AppConfig;
import commons.dto.ExecutionContext;
import commons.dto.HeaderParam;
import commons.exception.ServerException;
import org.apache.commons.lang3.StringUtils;
import telemetry.TelemetryManager;
import utils.CassandraConnector;
import utils.CassandraStoreParam;
import utils.Constants;
import utils.DateUtils;
import utils.KafkaUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

/**
 * @author pradyumna
 */
public abstract class CassandraStore {

    private String keyspace = null;
    private String table = null;
    private boolean index = false;
    private String objectType = null;
    protected String nodeType = CassandraStoreParam.EXTERNAL.name();
    private String DEFAULT_CHANNEL_ID = AppConfig.config.getString("channel.default");
    private Constants Constants;
    private static ObjectMapper mapper = new ObjectMapper();
    private static String topicName = AppConfig.getString("kafka.topic.graph_event", "sunbirddev.learning.graph.events");

    protected void initialise(String keyspace, String table, String objectType) {
        initialise(keyspace, table, objectType, false);
    }

    protected void initialise(String keyspace, String table, String objectType, boolean index) {
        this.keyspace = keyspace;
        this.table = table;
        this.objectType = objectType;
        this.index = index;
    }

    protected void insert(Object idValue, Map<String, Object> request) {
        try {
            if (null == request || request.isEmpty()) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid record to Insert.");
            }
            Set<String> keySet = request.keySet();
            String query = getPreparedStatement(keySet);
            Object[] objects = getBindObjects(request);
            executeQuery(query, objects);
            logTransactionEvent(CassandraStoreParam.CREATE.name(), idValue, request, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while inserting record", e);
        }
    }

    protected void update(String identifier, Object idValue, Map<String, Object> request, Map<String, Object> extEventData) {
        try {
            if (null == request || request.isEmpty()) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid record to Update.");
            }
            Set<String> keySet = request.keySet();
            String query = getUpdateQueryStatement(identifier, keySet);
            String updateQuery = query ;
            Object[] objects = new Object[request.size() + 1];
            Iterator<String> iterator = keySet.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                objects[i++] = request.get(iterator.next());
            }
            objects[i] = idValue;
            executeQuery(updateQuery, objects);
            logTransactionEvent(CassandraStoreParam.UPDATE.name(), idValue, request, extEventData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while updating record for id : " + idValue, e);
        }
    }

    protected void delete(String identifier, Object idValue) {
        try {
            if (StringUtils.isBlank(identifier)) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid Identifier to delete");
            }
            Delete.Where delete = QueryBuilder.delete().from(keyspace, table).where(eq(identifier, idValue));
            CassandraConnector.getSession().execute(delete);
            logTransactionEvent(CassandraStoreParam.DELETE.name(), identifier, null, null);
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while deleting record for id : " + idValue, e);
        }
    }

    protected List<Row> read(String key, Object value) {
        try {
            if (StringUtils.isBlank(key)) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid Identifier to read");
            }
            Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
            Where selectWhere = selectQuery.where();
            Clause clause = QueryBuilder.eq(key, value);
            selectWhere.and(clause);
            TelemetryManager.log("Cassandra store:: read query: " + selectQuery);
            ResultSet results = CassandraConnector.getSession().execute(selectQuery);
            return results.all();
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while fetching record for ID : " + value, e);
        }

    }

    protected List<Row> readByUUID(String key, Object value) {
        try {
            if (StringUtils.isBlank(key)) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid Identifier to read");
            }
            String selectQuery = "select * from " + keyspace+"."+table + " where " + key + "=" + value + ";";
            TelemetryManager.log("Cassandra store:: readByUUID query: " + selectQuery);
            ResultSet results = CassandraConnector.getSession().execute(selectQuery);
            TelemetryManager.log("Cassandra store:: readByUUID results: " + results.all());
            return results.all();
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while fetching record for ID : " + value, e);
        }

    }

    protected List<Row> getRecordsByProperty(String propertyName, List<Object> propertyValueList) {
        try {
            if (StringUtils.isBlank(propertyName) || propertyValueList.isEmpty()) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid propertyName to read");
            }
            Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
            Where selectWhere = selectQuery.where();
            Clause clause = QueryBuilder.in(propertyName, propertyValueList);
            selectWhere.and(clause);
            ResultSet results = CassandraConnector.getSession().execute(selectQuery);
            return results.all();
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while fetching record for Property : " + propertyName, e);
        }
    }

    protected List<Row> getRecordsByProperties(Map<String, Object> propertyMap) {
        try {
            if (null == propertyMap || propertyMap.isEmpty()) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid propertyName to read");
            }

            Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
            Where selectWhere = selectQuery.where();
            for (Entry<String, Object> entry : propertyMap.entrySet()) {
                if (entry.getValue() instanceof List) {
                    Clause clause = QueryBuilder.in(entry.getKey(), ((List) entry.getValue()).toArray());
                    selectWhere.and(clause);
                } else {
                    Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
                    selectWhere.and(clause);
                }
            }
            ResultSet results = CassandraConnector.getSession().execute(selectQuery.allowFiltering());
            return results.all();
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while fetching records", e);
        }
    }

    protected List<Row> getPropertiesValueById(String identifier, String idValue, String... properties) {
        try {
            if (StringUtils.isBlank(identifier)) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid Identifier to read");
            }
            String selectQuery = getSelectStatement(identifier, properties);
            PreparedStatement statement = CassandraConnector.getSession().prepare(selectQuery);
            BoundStatement boundStatement = new BoundStatement(statement);
            ResultSet results = CassandraConnector.getSession().execute(boundStatement.bind(identifier));
            return results.all();
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while fetching properties for ID : " + idValue, e);
        }
    }

    protected List<Row> getAllRecords() {
        try {
            Select selectQuery = QueryBuilder.select().all().from(keyspace, table);
            ResultSet results = CassandraConnector.getSession().execute(selectQuery);
            return results.all();
        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                    "Error while fetching all records", e);
        }
    }

    protected void upsertRecord(Map<String, Object> request) {
        try {
            if (null == request || request.isEmpty()) {
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid Identifier to read");
            }
            Session session = CassandraConnector.getSession();
            String query = getPreparedStatementFrUpsert(request);
            PreparedStatement statement = session.prepare(query);
            BoundStatement boundStatement = new BoundStatement(statement);
            Object[] objects = getBindObjects(request);
            session.execute(boundStatement.bind(objects));

        } catch (Exception e) {
            throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(), "Error while upsert record",
                    e);
        }
    }

    /**
     * @return the objectType
     */
    protected String getObjectType() {
        return objectType;
    }

    /**
     * @param objectType the objectType to set
     */
    protected void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * @return String String
     * @desc This method is used to create prepared statement based on table
     * name and column name provided in request
     */
    private String getPreparedStatement(Set<String> keySet) {
        StringBuilder query = new StringBuilder();
        query.append(Constants.INSERT_INTO + keyspace + Constants.DOT + table + Constants.OPEN_BRACE);
        query.append(String.join(",", keySet) + Constants.VALUES_WITH_BRACE);
        StringBuilder commaSepValueBuilder = new StringBuilder();
        for (int i = 0; i < keySet.size(); i++) {
            commaSepValueBuilder.append(Constants.QUE_MARK);
            if (i != keySet.size() - 1) {
                commaSepValueBuilder.append(Constants.COMMA);
            }
        }
        query.append(commaSepValueBuilder + ")");
        return query.toString();

    }

    /**
     * @return String String
     * @desc This method is used to create update query statement based on table
     * name and column name provided
     */
    private String getUpdateQueryStatement(String id, Set<String> key) {
        StringBuilder query = new StringBuilder(Constants.UPDATE + keyspace + Constants.DOT + table + Constants.SET);
        query.append(String.join(" = ? ,", key));
        query.append(Constants.EQUAL_WITH_QUE_MARK + Constants.WHERE + id + Constants.EQUAL_WITH_QUE_MARK);
        return query.toString();
    }

    /**
     * @param map is key value pair (key is column name and value is value of
     *            column)
     * @return String String
     * @desc This method is used to create prepared statement based on table
     * name and column name provided
     */
    private String getPreparedStatementFrUpsert(Map<String, Object> map) {
        StringBuilder query = new StringBuilder();
        query.append(Constants.INSERT_INTO + keyspace + Constants.DOT + table + Constants.OPEN_BRACE);
        Set<String> keySet = map.keySet();
        query.append(String.join(",", keySet) + Constants.VALUES_WITH_BRACE);
        StringBuilder commaSepValueBuilder = new StringBuilder();
        for (int i = 0; i < keySet.size(); i++) {
            commaSepValueBuilder.append(Constants.QUE_MARK);
            if (i != keySet.size() - 1) {
                commaSepValueBuilder.append(Constants.COMMA);
            }
        }
        query.append(commaSepValueBuilder + Constants.CLOSING_BRACE);
        return query.toString();

    }

    /**
     * @param properties(String varargs)
     * @return String String
     * @desc This method is used to create prepared statement based on table
     * name and column name provided as varargs
     */
    private String getSelectStatement(String identifier, String... properties) {
        StringBuilder query = new StringBuilder(Constants.SELECT);
        query.append(String.join(",", properties));
        query.append(Constants.FROM + keyspace + Constants.DOT + table + Constants.WHERE + identifier + Constants.EQUAL
                + " ?; ");
        return query.toString();
    }

    /**
     * @param query
     * @param objects
     * @return
     */
    private ResultSet executeQuery(String query, Object... objects) {
        Session session = CassandraConnector.getSession();
        PreparedStatement statement = session.prepare(query);
        BoundStatement boundStatement = new BoundStatement(statement);
        return session.execute(boundStatement.bind(objects));
    }

    private Object[] getBindObjects(Map<String, Object> request) {
        Set<String> keySet = request.keySet();
        Iterator<String> iterator = keySet.iterator();
        Object[] objects = new Object[keySet.size()];
        int i = 0;
        while (iterator.hasNext()) {
            objects[i++] = request.get(iterator.next());
        }
        return objects;
    }

    /**
     * @return the keyspace
     */
    protected String getKeyspace() {
        return keyspace;
    }

    /**
     * @return the table
     */
    protected String getTable() {
        return table;
    }

    protected void logTransactionEvent(String operation, Object identifier, Map<String, Object> map, Map<String, Object> extEventData) throws Exception {
        if (index) {
            if (null == map && !StringUtils.equalsIgnoreCase(operation, CassandraStoreParam.DELETE.name())) {
                TelemetryManager.log("Returning null as the map is is null", map);
                throw new ServerException(CassandraStoreParam.ERR_SERVER_ERROR.name(),
                        "Invalid request for LoggingTransactionEvent");
            } else {
                Map<String, Object> dataMap = new HashMap<String, Object>();
                Map<String, Object> transactionMap = new HashMap<String, Object>();
                Map<String, Object> propertiesMap = new HashMap<String, Object>();
                for (Entry<String, Object> entry : map.entrySet()) {
                    Map<String, Object> valueMap = new HashMap<String, Object>();
                    valueMap.put("ov", null);
                    valueMap.put("nv", entry.getValue());
                    TelemetryManager.log("Adding propertiesMap to log kafka message", valueMap);
                    propertiesMap.put(entry.getKey(), valueMap);
                }
                if (null != extEventData && !extEventData.isEmpty()) {
                        propertiesMap.putAll(extEventData);
                }
                transactionMap.put(CassandraStoreParam.properties.name(), propertiesMap);
                dataMap.put(CassandraStoreParam.transactionData.name(), transactionMap);
                dataMap.put(CassandraStoreParam.nodeUniqueId.name(), identifier);
                dataMap.put(CassandraStoreParam.requestId.name(), null);
                dataMap.put(CassandraStoreParam.operationType.name(), operation);
                dataMap.put(CassandraStoreParam.nodeType.name(), nodeType);
                dataMap.put(CassandraStoreParam.userId.name(), CassandraStoreParam.ANONYMOUS.name());
                dataMap.put(CassandraStoreParam.objectType.name(), objectType);
                dataMap.put(CassandraStoreParam.index.name(), index);
                dataMap.put(CassandraStoreParam.audit.name(), false);
                dataMap.put(CassandraStoreParam.ets.name(), System.currentTimeMillis());
                dataMap.put(CassandraStoreParam.createdOn.name(), DateUtils.format(new Date()));
                dataMap.put("channel", getChannel(DEFAULT_CHANNEL_ID));
                boolean txnEventEnabled = AppConfig.getBoolean("kafka.topic.send_enable", false);
                if(txnEventEnabled) KafkaUtils.send(mapper.writeValueAsString(dataMap), topicName);
            }
        }
    }

    private String getChannel(String defaultVal) {
        String channel = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.name());
        if (StringUtils.isBlank(channel))
            return defaultVal;
        else
            return channel;
    }
}
