package com.ursc.sandesh.claims.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for parsing dates in various formats.
 * Used during Excel import to normalise date values.
 */
public final class DateUtil {

    private static final String[] FORMATS = {
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "dd-MM-yyyy",
        "yyyy/MM/dd",
        "dd.MM.yyyy"
    };

    private DateUtil() {}

    /**
     * Attempts to parse a date string into yyyy-MM-dd format.
     * Returns empty string if parsing fails.
     */
    public static String parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String s = value.trim();

        for (String fmt : FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                sdf.setLenient(false);
                Date d = sdf.parse(s);
                return new SimpleDateFormat("yyyy-MM-dd").format(d);
            } catch (ParseException ignored) {}
        }

        // Excel serial date number (e.g. 45678)
        try {
            double n = Double.parseDouble(s);
            if (n > 20000 && n < 60000) {
                long millis = (long) ((n - 25569) * 86400000L);
                Date d = new Date(millis);
                return new SimpleDateFormat("yyyy-MM-dd").format(d);
            }
        } catch (NumberFormatException ignored) {}

        return s;
    }

    /**
     * Parses a number as a currency amount, stripping common symbols.
     */
    public static Double parseAmount(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString().trim()
            .replace(",", "")
            .replace("\u20B9", "")
            .replace("$", "")
            .replace(" ", "");
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
