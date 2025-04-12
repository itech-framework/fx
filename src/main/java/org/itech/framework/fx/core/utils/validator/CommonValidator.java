package org.itech.framework.fx.core.utils.validator;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class CommonValidator {
    public static boolean isValidObject(Object obj) {
        return obj != null;
    }

    public static boolean isValidDate(String dateStr, String dateFormat) {
        try {
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            df.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validInteger(Integer value) {
        return value != null && value > 0;
    }

    public static boolean validNumber(Number value) {
        return value != null && value.floatValue() > 0;
    }

    public static boolean validLong(Long value) {
        return value != null && value > 0;
    }

    public static boolean validDouble(Double value) {
        return value != null && value > 0;
    }

    public static boolean validBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean validString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean validList(Collection<?> value) {
        return value != null && !value.isEmpty();
    }

    public static boolean isNumeric(String strNum) {
        if (isEmpty(strNum)) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isValidLongValue(String strLong) {
        if (isEmpty(strLong)) {
            return false;
        }
        try {
            Long.parseLong(strLong);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.isEmpty();
    }

}
