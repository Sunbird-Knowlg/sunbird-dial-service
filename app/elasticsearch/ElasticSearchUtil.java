/**
 * 
 */
package elasticsearch;

import akka.dispatch.Futures;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AppConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import telemetry.TelemetryManager;
import utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author pradyumna
 *
 */
public class ElasticSearchUtil {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		registerShutdownHook();
	}

	private static Map<String, RestHighLevelClient> esClient = new HashMap<String, RestHighLevelClient>();

	public static int defaultResultLimit = 10000;
	private static final int resultLimit = 100;
	public int defaultResultOffset = 0;
	private static int BATCH_SIZE = (AppConfig.config.hasPath("search.batch.size"))
			? AppConfig.config.getInt("search.batch.size")
			: 1000;
	private static ObjectMapper mapper = new ObjectMapper();

	public static void initialiseESClient(String indexName, String connectionInfo) {
		if (StringUtils.isBlank(indexName))
			indexName = Constants.DIAL_CODE_INDEX;
		createClient(indexName, connectionInfo);
	}

	/**
	 * 
	 */
	private static void createClient(String indexName, String connectionInfo) {
		if (!esClient.containsKey(indexName)) {
			Map<String, Integer> hostPort = new HashMap<String, Integer>();
			for (String info : connectionInfo.split(",")) {
				hostPort.put(info.split(":")[0], Integer.valueOf(info.split(":")[1]));
			}
			List<HttpHost> httpHosts = new ArrayList<>();
			for (String host : hostPort.keySet()) {
				httpHosts.add(new HttpHost(host, hostPort.get(host)));
			}
			RestHighLevelClient client = new RestHighLevelClient(
					RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()])));
			if (null != client)
				esClient.put(indexName, client);
		}
	}

	private static RestHighLevelClient getClient(String indexName) {
		if (StringUtils.isBlank(indexName))
			indexName = Constants.DIAL_CODE_INDEX;
		return esClient.get(indexName);
	}

	public void finalize() {
		cleanESClient();
	}

	public static List<String> getQuerySearchFields() {
		List<String> querySearchFields = AppConfig.config.getStringList("search.fields.query");
		return querySearchFields;
	}

	public List<String> getDateFields() {
		List<String> dateFields = AppConfig.config.getStringList("search.fields.date");
		return dateFields;
	}

	public String getTimeZone() {
		String timeZoneProperty = AppConfig.config.getString("time-zone");
		if (timeZoneProperty == null) {
			timeZoneProperty = "0000";
		}
		return timeZoneProperty;
	}

	public static boolean isIndexExists(String indexName) {
		Response response;
		try {
			response = getClient(indexName).getLowLevelClient().performRequest("HEAD", "/" + indexName);
			return (200 == response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			return false;
		}

	}

	public static boolean addIndex(String indexName, String documentType, String settings, String mappings)
			throws IOException {
		boolean response = false;
		RestHighLevelClient client = getClient(indexName);
		if (!isIndexExists(indexName)) {
			CreateIndexRequest createRequest = new CreateIndexRequest(indexName);

			if (StringUtils.isNotBlank(settings))
				createRequest.settings(Settings.builder().loadFromSource(settings, XContentType.JSON));
			if (StringUtils.isNotBlank(documentType) && StringUtils.isNotBlank(mappings))
				createRequest.mapping(documentType, mappings, XContentType.JSON);
			CreateIndexResponse createIndexResponse = client.indices().create(createRequest);

			response = createIndexResponse.isAcknowledged();
		}
		return response;
	}

	public static void addDocumentWithId(String indexName, String documentType, String documentId, String document) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexResponse response = getClient(indexName)
					.index(new IndexRequest(indexName, documentType, documentId).source(doc));
			TelemetryManager.log("Added " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}

	public static void addDocument(String indexName, String documentType, String document) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexResponse response = getClient(indexName).index(new IndexRequest(indexName, documentType).source(doc));
			TelemetryManager.log("Added " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while adding document to index :" + indexName, e);
		}
	}

	public static void updateDocument(String indexName, String documentType, String document, String documentId)
			throws InterruptedException, ExecutionException {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexRequest indexRequest = new IndexRequest(indexName, documentType, documentId).source(doc);
			UpdateRequest request = new UpdateRequest().index(indexName).type(documentType).id(documentId).doc(doc)
					.upsert(indexRequest);
			UpdateResponse response = getClient(indexName).update(request);
			TelemetryManager.log("Updated " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			TelemetryManager.error("Error while updating document to index :" + indexName, e);
		}

	}

	public static void deleteDocument(String indexName, String documentType, String documentId)
			throws IOException {
		DeleteResponse response = getClient(indexName).delete(new DeleteRequest(indexName, documentType, documentId));
		TelemetryManager.log("Deleted " + response.getId() + " to index " + response.getIndex());
	}

	public static void deleteDocumentsByQuery(QueryBuilder query, String indexName, String indexType)
			throws IOException {
		Response response = getClient(indexName).getLowLevelClient().performRequest("POST",
				indexName + "/_delete_by_query" + query);

		TelemetryManager.log("Deleted Documents by Query" + EntityUtils.toString(response.getEntity()));
	}

	public static void deleteIndex(String indexName) throws InterruptedException, ExecutionException, IOException {
		DeleteIndexResponse response = getClient(indexName).indices().delete(new DeleteIndexRequest(indexName));
		esClient.remove(indexName);
		TelemetryManager.log("Deleted Index" + indexName + " : " + response.isAcknowledged());
	}

	public static String getDocumentAsStringById(String indexName, String documentType, String documentId)
			throws IOException {
		GetResponse response = getClient(indexName).get(new GetRequest(indexName, documentType, documentId));
		return response.getSourceAsString();
	}

	public static List<String> getMultiDocumentAsStringByIdList(String indexName, String documentType,
			List<String> documentIdList) throws IOException {
		List<String> finalResult = new ArrayList<String>();
		MultiGetRequest request = new MultiGetRequest();
		documentIdList.forEach(docId -> request.add(indexName, documentType, docId));
		MultiGetResponse multiGetItemResponses = getClient(indexName).multiGet(request);
		for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
			GetResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				finalResult.add(response.getSourceAsString());
			}
		}
		return finalResult;
	}

	@SuppressWarnings("unchecked")
	public static void bulkIndexWithIndexId(String indexName, String documentType, Map<String, Object> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			RestHighLevelClient client = getClient(indexName);
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				BulkRequest request = new BulkRequest();
				for (String key : jsonObjects.keySet()) {
					count++;
					request.add(new IndexRequest(indexName, documentType, key)
							.source((Map<String, Object>) jsonObjects.get(key)));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						BulkResponse bulkResponse = client.bulk(request);
						if (bulkResponse.hasFailures()) {
							TelemetryManager
									.log("Failures in Elasticsearch bulkIndex : " + bulkResponse.buildFailureMessage());
						}
					}
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	public static void bulkIndexWithAutoGenerateIndexId(String indexName, String documentType,
			List<Map<String, Object>> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			RestHighLevelClient client = getClient(indexName);
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				BulkRequest request = new BulkRequest();
				for (Map<String, Object> json : jsonObjects) {
					count++;
					request.add(new IndexRequest(indexName, documentType).source(json));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						BulkResponse bulkResponse = client.bulk(request);
						if (bulkResponse.hasFailures()) {
							TelemetryManager
									.log("Failures in Elasticsearch bulkIndex : " + bulkResponse.buildFailureMessage());
						}
					}
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> getDocumentsFromSearchResult(SearchResponse result, Class objectClass) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHits(hits);
	}

	public static List<Object> getDocumentsFromHits(SearchHits hits) {
		List<Object> documents = new ArrayList<Object>();
		for (SearchHit hit : hits) {
			documents.add(hit.getSourceAsMap());
		}
		return documents;
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getDocumentsFromSearchResultWithScore(SearchResponse result) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHitsWithScore(hits);
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getDocumentsFromHitsWithScore(SearchHits hits) {
		List<Map> documents = new ArrayList<Map>();
		for (SearchHit hit : hits) {
			Map<String, Object> hitDocument = hit.getSourceAsMap();
			hitDocument.put("score", hit.getScore());
			documents.add(hitDocument);
		}
		return documents;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Map> getDocumentsFromSearchResultWithId(SearchResponse result) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHitsWithId(hits);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Map> getDocumentsFromHitsWithId(SearchHits hits) {
		List<Map> documents = new ArrayList<Map>();
		for (SearchHit hit : hits) {
			Map<String, Object> hitDocument = (Map) hit.getSourceAsMap();
			hitDocument.put("id", hit.getId());
			documents.add(hitDocument);
		}
		return documents;
	}

	public static SearchResponse search(String indexName, String indexType, SearchSourceBuilder query)
			throws Exception {
		return getClient(indexName).search(new SearchRequest(indexName).source(query));
	}

	public static Future<SearchResponse> search(String indexName, SearchSourceBuilder searchSourceBuilder)
			throws IOException {
		TelemetryManager.log("searching in ES index: " + indexName);
		Promise<SearchResponse> promise = Futures.promise();
		getClient(indexName).searchAsync(new SearchRequest().indices(indexName).source(searchSourceBuilder),
				new ActionListener<SearchResponse>() {

					@Override
					public void onResponse(SearchResponse response) {
						promise.success(response);
					}

					@Override
					public void onFailure(Exception e) {
						promise.failure(e);
					}
				});
		return promise.future();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> getCountFromAggregation(Aggregations aggregations,
			List<Map<String, Object>> groupByList) {
		Map<String, Object> countMap = new HashMap<String, Object>();
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupByParent");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
				List<Map<String, Object>> parentGroupList = new ArrayList<Map<String, Object>>();
				for (Map aggKeyListMap : aggKeyList) {
					Map<String, Object> parentCountObject = new HashMap<String, Object>();
					parentCountObject.put("count", ((Double) aggKeyListMap.get("doc_count")).longValue());
					List<String> groupByChildList = (List<String>) aggregationsMap.get("groupByChildList");
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						Map<String, Object> groupByChildMap = new HashMap<String, Object>();
						for (String groupByChild : groupByChildList) {
							List<Map<String, Long>> childGroupsList = new ArrayList<Map<String, Long>>();
							Map aggChildKeyMap = (Map) aggKeyListMap.get(groupByChild);
							List<Map<String, Double>> aggChildKeyList = (List<Map<String, Double>>) aggChildKeyMap
									.get("buckets");
							Map<String, Long> childCountMap = new HashMap<String, Long>();
							for (Map aggChildKeyListMap : aggChildKeyList) {
								childCountMap.put((String) aggChildKeyListMap.get("key"),
										((Double) aggChildKeyListMap.get("doc_count")).longValue());
								childGroupsList.add(childCountMap);
								groupByChildMap.put(groupByChild, childCountMap);
							}
						}
						parentCountObject.putAll(groupByChildMap);
					}
					parentCountMap.put((String) aggKeyListMap.get("key"), parentCountObject);
					parentGroupList.add(parentCountMap);
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					cleanESClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void cleanESClient() {
		if (!esClient.isEmpty())
			for (RestHighLevelClient client : esClient.values()) {
				if (null != client)
					try {
						client.close();
					} catch (IOException e) {
					}
			}
	}

}
