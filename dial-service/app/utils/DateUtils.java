package utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static String format(Date date) {
        if (null != date) {
            try {
                return sdf.format(date);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Date parse(String dateStr) {
        if (StringUtils.isNotBlank(dateStr)) {
            try {
                return sdf.parse(dateStr);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static String formatCurrentDate() {
        return format(new Date());
    }
}
