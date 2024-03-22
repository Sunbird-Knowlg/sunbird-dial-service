package org.sunbird.elasticsearch;

import akka.dispatch.Mapper;
import org.sunbird.commons.AppConfig;
import org.sunbird.dto.SearchDTO;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import org.sunbird.telemetry.TelemetryManager;
import org.sunbird.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchProcessor {
	private static final String ASC_ORDER = "asc";
	private static final String AND = "AND";
	private boolean relevanceSort = false;

	public SearchProcessor() {
		ElasticSearchUtil.initialiseESClient(Constants.DIAL_CODE_INDEX,
				AppConfig.config.getString("search.es_conn_info"));
	}
	
	public void destroy() {
		// ElasticSearchUtil.cleanESClient();
	}

	/**
	 * @param searchDTO
	 * @param groupByFinalList
	 * @return
	 */
	private SearchSourceBuilder processSearchQuery(SearchDTO searchDTO, List<Map<String, Object>> groupByFinalList,
			boolean sortBy) {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		List<String> fields = searchDTO.getFields();
		if (null != fields && !fields.isEmpty()) {
			fields.add("objectType");
			fields.add("identifier");
			searchSourceBuilder.fetchSource(fields.toArray(new String[fields.size()]), null);
		}

		if (searchDTO.getFacets() != null && groupByFinalList != null) {
			for (String facet : searchDTO.getFacets()) {
				Map<String, Object> groupByMap = new HashMap<String, Object>();
				groupByMap.put("groupByParent", facet);
				groupByFinalList.add(groupByMap);
			}
		}

		searchSourceBuilder.size(searchDTO.getLimit());
		searchSourceBuilder.from(searchDTO.getOffset());
		QueryBuilder query = null;

		if (searchDTO.isFuzzySearch()) {
			query = prepareFilteredSearchQuery(searchDTO);
			relevanceSort = true;
		} else {
			query = prepareSearchQuery(searchDTO);
		}

		searchSourceBuilder.query(query);

		if (sortBy && !relevanceSort
				&& (null == searchDTO.getSoftConstraints() || searchDTO.getSoftConstraints().isEmpty())) {
			Map<String, String> sorting = searchDTO.getSortBy();
			if (sorting == null || sorting.isEmpty()) {
				sorting = new HashMap<String, String>();
				sorting.put("name", "asc");
				sorting.put("lastUpdatedOn", "desc");
			}
			for (String key : sorting.keySet())
				searchSourceBuilder.sort(key + Constants.RAW_FIELD_EXTENSION,
						getSortOrder(sorting.get(key)));
		}
		setAggregations(groupByFinalList, searchSourceBuilder);
		searchSourceBuilder.trackScores(true);
		return searchSourceBuilder;
	}

	/**
	 * @param groupByList
	 * @param searchSourceBuilder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void setAggregations(List<Map<String, Object>> groupByList,
			SearchSourceBuilder searchSourceBuilder) {
		TermsAggregationBuilder termBuilder = null;
		if (groupByList != null && !groupByList.isEmpty()) {
			for (Map<String, Object> groupByMap : groupByList) {
				String groupByParent = (String) groupByMap.get("groupByParent");
				termBuilder = AggregationBuilders.terms(groupByParent)
						.field(groupByParent + Constants.RAW_FIELD_EXTENSION)
						.size(ElasticSearchUtil.defaultResultLimit);
				List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
				if (groupByChildList != null && !groupByChildList.isEmpty()) {
					for (String childGroupBy : groupByChildList) {
						termBuilder.subAggregation(AggregationBuilders.terms(childGroupBy)
								.field(childGroupBy + Constants.RAW_FIELD_EXTENSION)
								.size(ElasticSearchUtil.defaultResultLimit));
					}
				}
				searchSourceBuilder.aggregation(termBuilder);
			}
		}
	}

	/**
	 * @param searchDTO
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private QueryBuilder prepareSearchQuery(SearchDTO searchDTO) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		QueryBuilder queryBuilder = null;
		String totalOperation = searchDTO.getOperation();
		List<Map> properties = searchDTO.getProperties();
		for (Map<String, Object> property : properties) {
			String opertation = (String) property.get("operation");

			List<Object> values;
			try {
				values = (List<Object>) property.get("values");
			} catch (Exception e) {
				values = Arrays.asList(property.get("values"));
			}
			values = values.stream().filter(value -> (null != value)).collect(Collectors.toList());


			String propertyName = (String) property.get("propertyName");
			if (propertyName.equals("*")) {
				relevanceSort = true;
				propertyName = "all_fields";
				queryBuilder = getAllFieldsPropertyQuery(values);
				boolQuery.must(queryBuilder);
				continue;
			}

			propertyName = propertyName + Constants.RAW_FIELD_EXTENSION;

			switch (opertation) {
			case Constants.SEARCH_OPERATION_EQUAL: {
				queryBuilder = getMustTermQuery(propertyName, values, true);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_EQUAL: {
				queryBuilder = getMustTermQuery(propertyName, values, false);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_IN: {
				queryBuilder = getNotInQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_ENDS_WITH: {
				queryBuilder = getRegexQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_LIKE:
			case Constants.SEARCH_OPERATION_CONTAINS: {
				queryBuilder = getMatchPhraseQuery(propertyName, values, true);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_LIKE: {
				queryBuilder = getMatchPhraseQuery(propertyName, values, false);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_STARTS_WITH: {
				queryBuilder = getMatchPhrasePrefixQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_EXISTS: {
				queryBuilder = getExistsQuery(propertyName, values, true);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_EXISTS: {
				queryBuilder = getExistsQuery(propertyName, values, false);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_GREATER_THAN: {
				queryBuilder = getRangeQuery(propertyName, values,
						Constants.SEARCH_OPERATION_GREATER_THAN);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				queryBuilder = getRangeQuery(propertyName, values,
						Constants.SEARCH_OPERATION_GREATER_THAN_EQUALS);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_LESS_THAN: {
				queryBuilder = getRangeQuery(propertyName, values, Constants.SEARCH_OPERATION_LESS_THAN);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				queryBuilder = getRangeQuery(propertyName, values,
						Constants.SEARCH_OPERATION_LESS_THAN_EQUALS);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			case Constants.SEARCH_OPERATION_RANGE: {
				queryBuilder = getRangeQuery(propertyName, values);
				queryBuilder = checkNestedProperty(queryBuilder, propertyName);
				break;
			}
			}
			if (totalOperation.equalsIgnoreCase(AND)) {
				boolQuery.must(queryBuilder);
			} else {
				boolQuery.should(queryBuilder);
			}

		}

		Map<String, Object> softConstraints = searchDTO.getSoftConstraints();
		if (null != softConstraints && !softConstraints.isEmpty()) {
			boolQuery.should(getSoftConstraintQuery(softConstraints));
			searchDTO.setSortBy(null);
			// relevanceSort = true;
		}
		return boolQuery;
	}

	private QueryBuilder checkNestedProperty(QueryBuilder queryBuilder, String propertyName) {
		if(propertyName.replaceAll(Constants.RAW_FIELD_EXTENSION, "").contains(".")) {
			queryBuilder = QueryBuilders.nestedQuery(propertyName.split("\\.")[0], queryBuilder, org.apache.lucene.search.join.ScoreMode.None);
		}
		return queryBuilder;
	}

	/**
	 * @param searchDTO
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private QueryBuilder prepareFilteredSearchQuery(SearchDTO searchDTO) {
		List<FilterFunctionBuilder> filterFunctionBuilder = new ArrayList<>();

		Map<String, Float> weightages = (Map<String, Float>) searchDTO.getAdditionalProperty("weightagesMap");
		if (weightages == null) {
			weightages = new HashMap<String, Float>();
			weightages.put("default_weightage", 1.0f);
		}
		List<String> querySearchFeilds = ElasticSearchUtil.getQuerySearchFields();
		List<Map> properties = searchDTO.getProperties();
		for (Map<String, Object> property : properties) {
			String opertation = (String) property.get("operation");

			List<Object> values;
			try {
				values = (List<Object>) property.get("values");
			} catch (Exception e) {
				values = Arrays.asList(property.get("values"));
			}

			values = values.stream().filter(value -> (null != value)).collect(Collectors.toList());
			String propertyName = (String) property.get("propertyName");
			if (propertyName.equals("*")) {
				relevanceSort = true;
				propertyName = "all_fields";
				filterFunctionBuilder
						.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(getAllFieldsPropertyQuery(values),
								ScoreFunctionBuilders.weightFactorFunction(weightages.get("default_weightage"))));
				continue;
			}

			propertyName = propertyName + Constants.RAW_FIELD_EXTENSION;
			float weight = getweight(querySearchFeilds, propertyName);
			switch (opertation) {
			case Constants.SEARCH_OPERATION_EQUAL: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMustTermQuery(propertyName, values, true),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_EQUAL: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMustTermQuery(propertyName, values, true),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_ENDS_WITH: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRegexQuery(propertyName, values), ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_LIKE:
			case Constants.SEARCH_OPERATION_CONTAINS: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMatchPhraseQuery(propertyName, values, true),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_LIKE: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMatchPhraseQuery(propertyName, values, false),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_STARTS_WITH: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getMatchPhrasePrefixQuery(propertyName, values),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_EXISTS: {
				filterFunctionBuilder.add(
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(getExistsQuery(propertyName, values, true),
								ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_EXISTS: {
				filterFunctionBuilder.add(
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(getExistsQuery(propertyName, values, false),
								ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_NOT_IN: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getNotInQuery(propertyName, values), ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_GREATER_THAN: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values, Constants.SEARCH_OPERATION_GREATER_THAN),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values,
								Constants.SEARCH_OPERATION_GREATER_THAN_EQUALS),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_LESS_THAN: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values, Constants.SEARCH_OPERATION_LESS_THAN),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			case Constants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				filterFunctionBuilder.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						getRangeQuery(propertyName, values, Constants.SEARCH_OPERATION_LESS_THAN_EQUALS),
						ScoreFunctionBuilders.weightFactorFunction(weight)));
				break;
			}
			}
		}

		FunctionScoreQueryBuilder queryBuilder = QueryBuilders
				.functionScoreQuery(
						filterFunctionBuilder.toArray(new FilterFunctionBuilder[filterFunctionBuilder.size()]))
				.boostMode(CombineFunction.REPLACE).scoreMode(ScoreMode.SUM);
		return queryBuilder;

	}

	/**
	 * @param querySearchFeilds
	 * @param propertyName
	 * @return
	 */
	private float getweight(List<String> querySearchFeilds, String propertyName) {
		float weight = 1.0F;
		if (querySearchFeilds.contains(propertyName)) {
			for (String field : querySearchFeilds) {
				if (field.contains(propertyName)) {
					weight = Float
							.parseFloat((StringUtils.isNotBlank(field.split("^")[1])) ? field.split("^")[1] : "1.0");
				}
			}
		}
		return weight;
	}

	/**
	 * @param values
	 * @return
	 */
	private QueryBuilder getAllFieldsPropertyQuery(List<Object> values) {
		List<String> queryFields = ElasticSearchUtil.getQuerySearchFields();
		Map<String, Float> queryFieldsMap = new HashMap<>();
		for (String field : queryFields) {
			if (field.contains("^"))
				queryFieldsMap.put(field.split("\\^")[0], Float.valueOf(field.split("\\^")[1]));
			else
				queryFieldsMap.put(field, 1.0f);
		}
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			queryBuilder
					.should(QueryBuilders.multiMatchQuery(value).fields(queryFieldsMap)
							.operator(Operator.AND).type(Type.CROSS_FIELDS).lenient(true));
		}

		return queryBuilder;
	}

	/**
	 * @param softConstraints
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private QueryBuilder getSoftConstraintQuery(Map<String, Object> softConstraints) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (String key : softConstraints.keySet()) {
			List<Object> data = (List<Object>) softConstraints.get(key);
			if(data.get(1) instanceof List) {
				List<Object> dataList = (List<Object>) data.get(1);
				for(Object value: dataList) {
					queryBuilder
							.should(QueryBuilders.matchQuery(key + Constants.RAW_FIELD_EXTENSION, value)
									.boost(Integer.valueOf((int) data.get(0)).floatValue()));
				}
			}
			else {
				queryBuilder.should(
						QueryBuilders.matchQuery(key + Constants.RAW_FIELD_EXTENSION, data.get(1))
								.boost(Integer.valueOf((int) data.get(0)).floatValue()));
			}
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @param opertation
	 * @return
	 */
	private QueryBuilder getRangeQuery(String propertyName, List<Object> values, String opertation) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			switch (opertation) {
			case Constants.SEARCH_OPERATION_GREATER_THAN: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).gt(value));
				break;
			}
			case Constants.SEARCH_OPERATION_GREATER_THAN_EQUALS: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).gte(value));
				break;
			}
			case Constants.SEARCH_OPERATION_LESS_THAN: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).lt(value));
				break;
			}
			case Constants.SEARCH_OPERATION_LESS_THAN_EQUALS: {
				queryBuilder.should(QueryBuilders
						.rangeQuery(propertyName).lte(value));
				break;
			}
			}
		}

		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @param exists
	 * @return
	 */
	private QueryBuilder getExistsQuery(String propertyName, List<Object> values, boolean exists) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			if (exists) {
				queryBuilder.should(QueryBuilders.existsQuery(String.valueOf(value)));
			} else {
				queryBuilder.mustNot(QueryBuilders.existsQuery(String.valueOf(value)));
			}
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getNotInQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder
				.mustNot(QueryBuilders.termsQuery(propertyName, values));
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getMatchPhrasePrefixQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			queryBuilder.should(QueryBuilders.prefixQuery(
					propertyName, ((String) value).toLowerCase()));
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @param match
	 * @return
	 */
	private QueryBuilder getMatchPhraseQuery(String propertyName, List<Object> values, boolean match) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			String stringValue = String.valueOf(value);
			if (match) {
				queryBuilder.should(QueryBuilders
						.regexpQuery(propertyName,
								".*" + stringValue.toLowerCase() + ".*"));
			} else {
				queryBuilder.mustNot(QueryBuilders
						.regexpQuery(propertyName,
								".*" + stringValue.toLowerCase() + ".*"));
			}
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	private QueryBuilder getRegexQuery(String propertyName, List<Object> values) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			String stringValue = String.valueOf(value);
			queryBuilder.should(QueryBuilders.regexpQuery(propertyName,
					".*" + stringValue.toLowerCase()));
		}
		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @param match
	 * @return
	 */
	private QueryBuilder getMustTermQuery(String propertyName, List<Object> values, boolean match) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		for (Object value : values) {
			if (match) {
				queryBuilder.should(
						QueryBuilders.matchQuery(propertyName, value));
			} else {
				queryBuilder.mustNot(
						QueryBuilders.matchQuery(propertyName, value));
			}
		}

		return queryBuilder;
	}

	/**
	 * @param propertyName
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private QueryBuilder getRangeQuery(String propertyName, List<Object> values) {
		RangeQueryBuilder queryBuilder = new RangeQueryBuilder(propertyName);
		for (Object value : values) {
			Map<String, Object> rangeMap = (Map<String, Object>) value;
			if (!rangeMap.isEmpty()) {
				for (String key : rangeMap.keySet()) {
					switch (key) {
					case Constants.SEARCH_OPERATION_RANGE_GTE: {
						queryBuilder.from(rangeMap.get(key));
						break;
					}
					case Constants.SEARCH_OPERATION_RANGE_LTE: {
						queryBuilder.to(rangeMap.get(key));
						break;
					}
					}
				}
			}
		}
		return queryBuilder;
	}

	/**
	 * @param value
	 * @return
	 */
	private SortOrder getSortOrder(String value) {
		return value.equalsIgnoreCase(ASC_ORDER) ? SortOrder.ASC : SortOrder.DESC;
	}

	public Future<List<Object>> processSearchQuery(SearchDTO searchDTO, boolean includeResults, String index,
			boolean sort)
			throws Exception {
		List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
		if (searchDTO.getLimit() == 0)
			searchDTO.setLimit(ElasticSearchUtil.defaultResultLimit);
		SearchSourceBuilder query = processSearchQuery(searchDTO, groupByFinalList, sort);
		TelemetryManager.log(" search query: " + query);
		Future<SearchResponse> searchResponse = ElasticSearchUtil.search(index, query);
		
		return searchResponse.map(new Mapper<SearchResponse, List<Object>>() {
			public List<Object> apply(SearchResponse searchResult) {
				List<Object> response = new ArrayList<Object>();
				TelemetryManager.log("search result from elastic search" + searchResult);
				SearchHits resultMap = searchResult.getHits();
				SearchHit[] result = resultMap.getHits();
				for (SearchHit hit : result) {
					response.add(hit.getSourceAsMap());
				}
				TelemetryManager.log("search response size: " + response.size());
				return response;
			}
		}, ExecutionContext.Implicits$.MODULE$.global());
		
	}

	public Future<SearchResponse> processSearchQueryWithSearchResult(SearchDTO searchDTO, boolean includeResults,
			String index,
			boolean sort) throws Exception {
		List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
		if (searchDTO.getLimit() == 0)
			searchDTO.setLimit(ElasticSearchUtil.defaultResultLimit);
		SearchSourceBuilder query = processSearchQuery(searchDTO, groupByFinalList, sort);
		TelemetryManager.log(" search query: " + query);
		Future<SearchResponse> searchResult = ElasticSearchUtil.search(index, query);
		TelemetryManager.log("search result from elastic search" + searchResult);
		return searchResult;
	}

}