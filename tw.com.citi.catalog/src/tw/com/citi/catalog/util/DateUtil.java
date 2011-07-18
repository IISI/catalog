package tw.com.citi.catalog.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static final String FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
        return sdf.format(date);
    }

    public static String format(long millisecond) {
        return format(new Date(millisecond));
    }

    public static String format(Timestamp timestamp) {
        return format(timestamp.getTime());
    }

}
