package utils;

import commons.AppConfig;
import dbstore.DialCodeStore;
import dbstore.SystemConfigStore;
import org.apache.commons.lang3.StringUtils;
import telemetry.TelemetryManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialCodeGenerator {

	private DialCodeStore dialCodeStore =  new DialCodeStore();

	private SystemConfigStore systemConfigStore = new SystemConfigStore();

	private static String stripChars = AppConfig.getString("dialcode.strip.chars", "0");
	private static Double length = AppConfig.getDouble("dialcode.length", 6.0);
	private static BigDecimal largePrimeNumber = new BigDecimal(1679979167);
	private static String regex = "[A-Z][0-9][A-Z][0-9][A-Z][0-9]";
	private static Pattern pattern = Pattern.compile(regex);

	private static final String[] alphabet = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z" };

	public Map<Double, String> generate(double count, String channel, String publisher, String batchCode)
			throws Exception {
		Map<Double, String> codes = new HashMap<Double, String>();
		int totalChars = alphabet.length;
		BigDecimal exponent = BigDecimal.valueOf(totalChars);
		exponent = exponent.pow(length.intValue());
		double codesCount = 0;
		double lastIndex = systemConfigStore.getDialCodeIndex();
		while (codesCount < count) {
			lastIndex = getMaxIndex(lastIndex);
			BigDecimal number = new BigDecimal(lastIndex);
			BigDecimal num = number.multiply(largePrimeNumber).remainder(exponent);
			String code = baseN(num, totalChars);
			if (code.length() == length && isValidCode(code)) {
				try {
					dialCodeStore.save(channel, publisher, batchCode, code, lastIndex);
					codesCount += 1;
					codes.put(lastIndex, code);
				} catch (Exception e) {
					TelemetryManager.error("Error while generating DIAL code", e);
					throw e;
				}
			}
		}
		setMaxIndex(lastIndex);
		return codes;
	}

	private static String baseN(BigDecimal num, int base) {
		if (num.doubleValue() == 0) {
			return "0";
		}
		double div = Math.floor(num.doubleValue() / base);
		String val = baseN(new BigDecimal(div), base);
		return StringUtils.stripStart(val, stripChars) + alphabet[num.remainder(new BigDecimal(base)).intValue()];
	}

	private void setMaxIndex(double maxIndex) throws Exception {
        System.out.println("Setting max index to cassandra: " + maxIndex);
		systemConfigStore.setDialCodeIndex(maxIndex);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Double getMaxIndex(Double masterDBIndex) throws Exception {
		double index = RedisStoreUtil.getNodePropertyIncVal("domain", "dialcode", "max_index");
		if (index < masterDBIndex) {
		    String message = "Redis doesn't have the latest max index. Please set max index in redis as : " + masterDBIndex + " to enable the service.";
            System.out.println(message);
            managers.HealthCheckManager.dialMaxIndexHealth = false;
            throw new Exception(message);
            // TODO: below code will be helpful for automating the Redis data correction.
		    // RedisStoreUtil.saveNodeProperty("domain", "dialcode", "max_index", masterDBIndex.toString());
		    // index = RedisStoreUtil.getNodePropertyIncVal("domain", "dialcode", "max_index");
        }
		return index;
	}

	/**
	 * This Method will check if dialcode has numeric value at odd indexes.
	 * @param code
	 * @return Boolean
	 */
	private Boolean isValidCode(String code) {
		Matcher matcher = pattern.matcher(code);
		return matcher.matches();
	}

}
