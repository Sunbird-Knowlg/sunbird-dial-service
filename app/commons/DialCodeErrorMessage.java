/**
 * 
 */
package commons;

/**
 * @author gauraw
 *
 */
public class DialCodeErrorMessage {
	public static final String ERR_SERVICE_UNAVAILABLE = "Services are temporarily unavailable, please try again later";
	public static final String ERR_SERVER_ERROR = "Something Went Wrong While Processing Your Request.";
	public static final String ERR_INVALID_DIALCODE_REQUEST = "Invalid Request.";
	public static final String ERR_INVALID_PUBLISHER = "Invalid Publisher. Please Provide Correct Publisher Details.";
	public static final String ERR_INVALID_COUNT = "Please Provide Valid Number Of DIAL Codes.";
	public static final String ERR_COUNT_VALIDATION_FAILED = "Maximum Number Of Allowed DIAL Code is 1000.";
	public static final String ERR_INVALID_CHANNEL_ID = "Invalid Channel Id.";
	public static final String ERR_DIALCODE_UPDATE = "Dial Code with Live status can't be updated.";
	public static final String ERR_INVALID_SEARCH_REQUEST = "Invalid Search Request";
	public static final String ERR_DIALCODE_INFO = "Dial Code Not Found With Id: ";
	public static final String ERR_INVALID_DIALCODE_LINK_REQUEST = "Invalid Request. Multiple Dial Codes Can Not be Linked With Multiple Contents";
	public static final String ERR_DIALCODE_LINK_REQUEST = "Invalid Request.";
	public static final String ERR_DIALCODE_LINK = "Internal Server Error.";
	public static final String ERR_INVALID_SYNC_REQUEST = "Please specify the Dial Code IDs to be synced";
	public static final String ERR_SCHEMA_VALIDATION_FAILED = "ContentInfo schema validation failed: ";
	public static final String ERR_CONTEXT_TYPE_MANDATORY = "Please specify the context 'type' in the 'contextInfo'.";
	public static final String ERR_CONTEXT_INFO_MANDATORY = "Please specify the 'contextInfo'.";
	public static final String ERR_TYPE_SCHEMA_MISSING = "Please provide valid url of context type's schema.json.";
	public static final String ERR_TYPE_CONTEXT_MISSING = "Please provide valid url of context type's context.json.";
	public static final String ERR_SCHEMA_BASEPATH_CONFIG_MISSING = "Please configure valid url in schema.basePath.";
}
