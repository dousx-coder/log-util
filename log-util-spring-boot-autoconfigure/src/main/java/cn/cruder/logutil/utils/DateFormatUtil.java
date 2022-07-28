package cn.cruder.logutil.utils;

import cn.cruder.logutil.enums.DatePattern;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dousx
 * @date 2022-07-02 14:47
 */
public final class DateFormatUtil {
    private DateFormatUtil() {
    }

    private final static DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DatePattern.NORM_DATETIME_MS_PATTERN.pattern());

    public synchronized static String format(Date date) {
        return SIMPLE_DATE_FORMAT.format(date);
    }
}
