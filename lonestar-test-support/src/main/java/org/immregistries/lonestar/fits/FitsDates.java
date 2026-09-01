package org.immregistries.lonestar.fits;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FitsDates {
    private FitsDates() {
    }

    public static Date parse(String value) {
        try {
            return format().parse(value);
        } catch (ParseException exception) {
            throw new IllegalArgumentException("Not a yyyy-MM-dd date: " + value, exception);
        }
    }

    public static String format(Date value) {
        return value == null ? null : format().format(value);
    }

    private static SimpleDateFormat format() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        return format;
    }
}