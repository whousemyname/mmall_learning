package com.gogotao.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateTimeUtils {

    private static final String STANDBY_PATTERN = "yyyy-MM-dd HH-mm-SS";

    public static Date strToDate(String dateStr, String pattern){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    public static String dateToString(Date date, String pattern){
        if (date == null){
            return StringUtils.EMPTY;
        }
        return new DateTime(date).toString(pattern);
    }

    public static Date strToDate(String dateStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDBY_PATTERN);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    public static String dateToString(Date date){
        if (date == null){
            return StringUtils.EMPTY;
        }
        return new DateTime(date).toString(STANDBY_PATTERN);
    }
}
