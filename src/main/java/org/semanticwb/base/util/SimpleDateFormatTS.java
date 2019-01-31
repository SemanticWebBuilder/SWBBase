package org.semanticwb.base.util;

import java.text.ParseException;
import java.util.Date;

/**
 * @deprecated Use {@link java.time.format.DateTimeFormatter} instead.
 * Implementation of thread safe SimpleDateFormat wrapper.
 * @author javier.solis.g
 */
//TODO: Remove this class and refactor SWBUtils to use java 8 DateTimeFormatter class
@Deprecated
public class SimpleDateFormatTS {
    private final ThreadLocal<java.text.SimpleDateFormat> sd;

    /**
     * Creates a new thread safe {@link SimpleDateFormatTS} object.
     * @param patern DateTime pattern.
     */
    public SimpleDateFormatTS(final String patern) {
        sd = new ThreadLocal<java.text.SimpleDateFormat>() {
            @Override
            protected java.text.SimpleDateFormat initialValue()
            {
                return new java.text.SimpleDateFormat(patern);
            }
        };            
    }

    /**
     * Parses a date string.
     * @param txt Date string.
     * @return Parsed {@link Date} object.
     * @throws ParseException if the provided date string does not match pattern.
     */
    public Date parse(String txt) throws ParseException {
        return sd.get().parse(txt);
    }

    /**
     * Formats a date-time object.
     * @param date {@link Date} object to format.
     * @return String representation of date object matching pattern.
     */
    public String format(Date date) {
        return sd.get().format(date);
    }
}
