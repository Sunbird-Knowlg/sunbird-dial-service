package manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.CassandraTestSetup;
import commons.AppConfig;
import commons.dto.Response;
import commons.exception.ClientException;
import elasticsearch.ElasticSearchUtil;
import managers.DialcodeManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import utils.Constants;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author gauraw
 *
 */
public class DialCodeManagerImplTest extends CassandraTestSetup {

	private DialcodeManager dialCodeMgr = new DialcodeManager();

	private static String dialCode = "";
	private static String publisherId = "";
	private static ObjectMapper mapper = new ObjectMapper();
	private static String DIALCODE_INDEX = "testdialcode";
	private static Pattern pattern;

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS dialcode_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.system_config_test (prop_key text,prop_value text,primary key(prop_key));";
	private static String cassandraScript_3 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.dial_code_test (identifier text,dialcode_index double,publisher text,channel text,batchCode text,metadata text,status text,generated_on text,published_on text, primary key(identifier));";
	private static String cassandraScript_4 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.publisher (identifier text,name text,channel text,created_on text,updated_on text,primary key(identifier));";
	private static String cassandraScript_5 = "INSERT INTO dialcode_store_test.system_config_test(prop_key,prop_value) values('dialcode_max_index','1');";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setup() throws Exception {
		String regex = "[A-Z][0-9][A-Z][0-9][A-Z][0-9]";
		pattern = Pattern.compile(regex);
		executeScript(cassandraScript_1, cassandraScript_2, cassandraScript_3, cassandraScript_4, cassandraScript_5);
		createDialCodeIndex();
	}

	@AfterClass
	public static void finish() throws InterruptedException, ExecutionException, IOException {
		ElasticSearchUtil.deleteIndex(DIALCODE_INDEX);
	}

	@Before
	public void init() throws Exception {

		if (StringUtils.isBlank(publisherId))
			createPublisher();
		if (StringUtils.isBlank(dialCode))
			generateDIALCode();

	}

	private void generateDIALCode() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\",\"batchCode\":\"test_math_std1\"}";
		String channelId = "channelTest";
		Response resp = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
		@SuppressWarnings("unchecked")
		Collection<String> obj = (Collection) resp.getResult().get("dialcodes");
		for (String s : obj) {
			dialCode = s;
		}
	}

	private void createPublisher() throws Exception {
		String createPublisherReq = "{\"identifier\":\"mock_pub01\",\"name\": \"Mock Publisher 1\"}";
		String channelId = "channelTest";
		Response resp = dialCodeMgr.createPublisher(getRequestMap(createPublisherReq), channelId);
		publisherId = (String) resp.get("identifier");
	}

	@Test
	public void dialCodeTest_27() throws Exception {
		Response response = dialCodeMgr.readPublisher("mock_pub01");
		assertTrue(response.getResponseCode().toString().equals("OK"));
		assertTrue(response.getResponseCode().code() == 200);
	}

	@Test
	public void dialCodeTest_28() throws Exception {
		Response response = dialCodeMgr.readDialCode(dialCode);
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}
	@Test
	public void dialCodeTest_01() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
		assertTrue(response.getResponseCode().toString().equals("OK"));
		assertTrue(response.getResponseCode().code() == 200);
	}

	@Test
	public void dialCodeTest_001() throws Exception {
		String dialCodeGenReq = "{\"count\":1}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
		assertTrue(response.getResponseCode().toString().equals("OK"));
		assertTrue(response.getResponseCode().code() == 200);
	}

	@Test
	public void dialCodeTest_02() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
	}

	@Test
	public void dialCodeTest_03() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
	}

	@Test
	public void dialCodeTest_04() throws Exception {
		String dialCodeId = null;
		Response response = dialCodeMgr.readDialCode(dialCodeId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}


	// Publish Dial Code with Different Channel Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_05() throws Exception {
		String channelId = "channelABC";
		Response response = dialCodeMgr.publishDialCode(dialCode, channelId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Publish Dial Code with Same Channel Id - 200 - OK
	@Test
	public void dialCodeTest_06() throws Exception {
		String channelId = "channelTest";
		Response response = dialCodeMgr.publishDialCode(dialCode, channelId);
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	// Update Dial Code with Different Channel Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_07() throws Exception {
		String dialCodeUpdateReq = "{\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}";
		String channelId = "channelABC";
		Response response = dialCodeMgr.updateDialCode(dialCode, channelId, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Update Dial Code having Live Status - CLIENT_ERROR
	@Test
	public void dialCodeTest_08() throws Exception {
		String dialCodeUpdateReq = "{\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.updateDialCode(dialCode, channelId, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// List Dial Code without Publisher - OK
	@Test
	public void dialCodeTest_09() throws Exception {
		String listReq = "{\"status\":\"Live\"}";
		Response response = dialCodeMgr.listDialCode(getRequestContext(), getRequestMap(listReq));
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	// Search Dial Code with null map - CLIENT_ERROR
	@Test
	public void dialCodeTest_10() throws Exception {
		Response response = dialCodeMgr.searchDialCode(getRequestContext(), null, null);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Search Dial Code with Invalid limit - CLIENT_ERROR
	@Test
	public void dialCodeTest_11() throws Exception {
		exception.expect(ClientException.class);
		String searchReq = "{\"status\":\"Live\",\"limit\":\"abc\"}";
		Response response = dialCodeMgr.searchDialCode(getRequestContext(), getRequestMap(searchReq),null);
	}

	// Sync Dial Code with null Request - CLIENT_ERROR
	@Test
	public void dialCodeTest_12() throws Exception {
		String channelId = "channelTest";
		Response response = dialCodeMgr.syncDialCode(channelId, null, null);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Create Publisher without Name - CLIENT_ERROR
	@Test
	public void dialCodeTest_13() throws Exception {
		String createPubReq = "{\"identifier\":\"mock_pub01\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.createPublisher(getRequestMap(createPubReq), channelId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Create Publisher with Invalid Name - CLIENT_ERROR
	@Test
	public void dialCodeTest_14() throws Exception {
		String createPubReq = "{\"identifier\":\"mock_pub01\",\"name\":\"\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.createPublisher(getRequestMap(createPubReq), channelId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Read Publisher with Blank Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_15() throws Exception {
		Response response = dialCodeMgr.readPublisher("");
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Update Publisher with null metadata - CLIENT_ERROR
	@Test
	public void dialCodeTest_16() throws Exception {
		Response response = dialCodeMgr.updatePublisher("ABC", "ABC", null);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Generate DIAL Code with Invalid Count - Client Exception
	@Test
	public void dialCodeTest_17() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":\"ABC\",\"publisher\": \"mock_pub01\",\"batchCode\":\"test_math_std1\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
	}

	// Generate DIAL Code with Invalid Count (Integer but -ve number) -
	// CLIENT_ERROR
	@Test
	public void dialCodeTest_18() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":-2,\"publisher\": \"mock_pub01\",\"batchCode\":\"test_math_std1\"}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
	}


	//Read DIAL code with dialCode as null - CLIENT ERROR
	@Test
	public void dialCodeTest_19() throws Exception {
		String dialCodeId = null;
		Response response = dialCodeMgr.readDialCodeV4(dialCodeId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Update Dial Code with Different Channel Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_20() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\",\"batchCode\":\"v4_check\"}";
		String channelId = "channelTest";
		Response resp = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);

		Collection<String> obj = (Collection) resp.getResult().get("dialcodes");
		for (String s : obj) {
			dialCode = s;
		}
		String dialCodeUpdateReq = "{\"dialcode\": {\"publisher\": \"testPublisherUpdated\",\"contextInfo\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}";
		String channelIdWrong = "channelABC";
		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelIdWrong, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}


	// Update Dial Code with status Live - CLIENT_ERROR
	@Test
	public void dialCodeTest_21() throws Exception {
		String channelId = "channelTest";
		dialCodeMgr.publishDialCode(dialCode, channelId);
		String dialCodeUpdateReq = "{\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"contextInfo\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}";
		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelId, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}


	// Update Dial Code without contextInfo - OK
	@Test
	public void dialCodeTest_22() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\",\"batchCode\":\"v4_check\"}";
		String channelId = "channelTest";
		Response resp = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);

		Collection<String> obj = (Collection) resp.getResult().get("dialcodes");
		for (String s : obj) {
			dialCode = s;
		}

		String dialCodeUpdateReq = "{\"publisher\": \"testPublisherUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}";
		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelId, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	// Update Dial Code without type in contextInfo - CLIENT_ERROR
	@Test
	public void dialCodeTest_23() throws Exception {
		String dialCodeUpdateReq = "{\"publisher\": \"testPublisherUpdated\",\"contextInfo\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelId, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Update Dial Code with contextInfo - OK
	@Test
	public void dialCodeTest_24() throws Exception {
		String dialCodeUpdateReq = "{\"contextInfo\":{\"@type\":\"sb-ed:TextBookUnit\",\"identifier\":\"do_31307361357558579213961\",\"name\":\"1-झूला\",\"parentInfo\":{\"name\":\"(NEW) रिमझिम\",\"identifier\":\"do_31307361357388185614238\",\"primaryCategory\":\"Digital Textbook\",\"framework\":{\"subject\":[\"Hindi\"],\"identifier\":\"do_31307361357388185614238\",\"medium\":[\"English\",\"Hindi\"]},\"@type\":\"sb-ed:TextBook\"}}}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = getRequestMap(dialCodeUpdateReq);

		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelId, requestMap);
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	// Update Dial Code with contextInfo and generatedDIAL code - OK
	@Test
	public void dialCodeTest_25() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\",\"batchCode\":\"v4_check\"}";
		String channelId = "channelTest";
		Response resp = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);

		Collection<String> obj = (Collection) resp.getResult().get("dialcodes");
		for (String s : obj) {
			dialCode = s;
		}

		String dialCodeUpdateReq = "{\"contextInfo\":{\"@type\":\"sb-ed:TextBookUnit\",\"identifier\":\"do_31307361357558579213961\",\"name\":\"1-झूला\",\"parentInfo\":{\"name\":\"(NEW) रिमझिम\",\"identifier\":\"do_31307361357388185614238\",\"primaryCategory\":\"Digital Textbook\",\"framework\":{\"subject\":[\"Hindi\"],\"identifier\":\"do_31307361357388185614238\",\"medium\":[\"English\",\"Hindi\"]},\"@type\":\"sb-ed:TextBook\"}}}";
		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelId, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	//Read DIAL code with valid dialCode having contextInfo - OK
	@Test
	public void dialCodeTest_26() throws Exception {
		Response response = dialCodeMgr.readDialCodeV4(dialCode);
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}


	// Update Dial Code with metadata - OK
	@Test
	public void dialCodeTest_29() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\",\"batchCode\":\"v4_check\"}";
		String channelId = "channelTest";
		Response resp = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);

		Collection<String> obj = (Collection) resp.getResult().get("dialcodes");
		for (String s : obj) {
			dialCode = s;
		}
		String dialCodeUpdateReq = "{\"publisher\": \"testPublisherUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}";
		String channelIdWrong = "channelTest";
		Response response = dialCodeMgr.updateDialCode(dialCode, channelIdWrong, getRequestMap(dialCodeUpdateReq));
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	// Update Dial Code with contextInfo as null - OK
	@Test
	public void dialCodeTest_30() throws Exception {
		String channelId = "channelTest";
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("contextInfo", null);

		Response response = dialCodeMgr.updateDialCodeV4(dialCode, channelId, requestMap);
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	@Test
	public void validateAdopterContext () throws Exception {
		Response response = dialCodeMgr.validateContextVocabulary("https://raw.githubusercontent.com/project-sunbird/sunbird-dial-service/release-5.0.0/test/resources/context.json");
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	@Test
	public void generateDialCodeExpectValidUniqueDialCodes() throws Exception {
		String dialCodeGenReq = "{\"count\":900}";
		String channelId = "channelTest";
		Response response = dialCodeMgr.generateDialCode(getRequestMap(dialCodeGenReq), channelId);
		assertTrue(response.getResponseCode().toString().equals("OK"));
		assertTrue(response.getResponseCode().code() == 200);
		Collection<String> obj = (Collection) response.getResult().get("dialcodes");
		Set<String> dialcodes = new HashSet<String>(obj);
		if (null != dialcodes && !dialcodes.isEmpty())
			assertEquals(900, dialcodes.size());
		assertTrue(validateDialCodes(obj));
	}


	private static void createDialCodeIndex() throws IOException {
		Constants.DIAL_CODE_INDEX=DIALCODE_INDEX;
		ElasticSearchUtil.initialiseESClient(DIALCODE_INDEX, AppConfig.config.getString("search.es_conn_info"));
		String settings = "{\"analysis\": {       \"analyzer\": {         \"dc_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"dc_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"dc_index_analyzer\",\"search_analyzer\":\"dc_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"dc_index_analyzer\",\"search_analyzer\":\"dc_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(DIALCODE_INDEX,
				Constants.DIAL_CODE_INDEX_TYPE, settings, mappings);

		populateData();
	}

	/**
	 *
	 * @param request
	 * @return requestMap
	 * @throws Exception
	 */
	private static Map<String, Object> getRequestMap(String request) throws Exception {
		return mapper.readValue(request, new TypeReference<Map<String, Object>>() {
		});
	}

	/**
	 * This Method Returns Request Context Map.
	 * @return Map
	 */
	private static Map<String, Object> getRequestContext() {
		return new HashMap<String, Object>() {{
			put("CHANNEL_ID", "channelTest");
		}};
	}

	/**
	 *
	 * @param dialcodes
	 * @return Boolean
	 */
	private static Boolean validateDialCodes(Collection<String> dialcodes) {
		Boolean isValid = true;
		for (String str : dialcodes) {
			if (!pattern.matcher(str).matches()) {
				isValid = false;
				break;
			}
		}
		return isValid;
	}

	private static void populateData() throws JsonProcessingException, IOException {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		indexDocument.put("dialcode_index", 1);
		indexDocument.put("identifier", dialCode);
		indexDocument.put("channel", "channelTest");
		indexDocument.put("batchcode", "test_math_std1");
		indexDocument.put("publisher", "mock_pub01");
		indexDocument.put("status", "Draft");
		indexDocument.put("generated_on", "2018-01-30T16:50:40.562");
		indexDocument.put("index", "true");
		indexDocument.put("operationType", "CREATE");
		indexDocument.put("nodeType", "EXTERNAL");
		indexDocument.put("userId", "ANONYMOUS");
		indexDocument.put("createdOn", "2018-01-30T16:50:40.593+0530");
		indexDocument.put("userId", "ANONYMOUS");
		indexDocument.put("objectType", "DialCode");

		ElasticSearchUtil.addDocumentWithId(DIALCODE_INDEX, Constants.DIAL_CODE_INDEX_TYPE, dialCode,
				mapper.writeValueAsString(indexDocument));
	}
}
