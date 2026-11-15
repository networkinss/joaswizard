package ch.inss.openapi.joaswizard.application;

import ch.inss.openapi.joaswizard.domain.Property;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Infers the OAS type of a property from its parsed sample value (Yaml/JSON input, SnakeYAML SafeConstructor).
 * <ul>
 * <li>Integer, Long, BigInteger: integer (format int64 if the value does not fit an int); Double, Float, BigDecimal: number.</li>
 * <li>Boolean: boolean. String: string with minLength of the trimmed text (a quoted "5" stays a string).</li>
 * <li>Date: string with format date (UTC midnight, i.e. no time part) or date-time; example in ISO form.</li>
 * <li>null: string without example and minLength.</li>
 * <li>List: array; items integer, number, boolean, string (mixed) or object (contains maps, no properties yet).
 * Empty list: string items without example.</li>
 * <li>Map and anything else: string of the text representation. The source turns non-empty maps and lists of maps into
 * nested schemas before calling this method, so this only sees empty maps and lists mixing maps with other values.</li>
 * </ul>
 */
public final class TypeInferrer {

    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;

    /**
     * @param key      property name.
     * @param rawValue sample value as parsed from Yaml, may be null.
     * @return the property with inferred type, example and length.
     */
    public Property infer(String key, Object rawValue) {
        if (rawValue == null) {
            return scalar(key, "string", null, null, null);
        }
        if (rawValue instanceof Boolean) {
            return scalar(key, "boolean", rawValue.toString(), null, null);
        }
        if (rawValue instanceof Integer || rawValue instanceof Short || rawValue instanceof Byte) {
            return scalar(key, "integer", rawValue.toString(), null, null);
        }
        if (rawValue instanceof Long) {
            long l = (Long) rawValue;
            return scalar(key, "integer", rawValue.toString(), null, l == (int) l ? null : "int64");
        }
        if (rawValue instanceof BigInteger) {
            BigInteger b = (BigInteger) rawValue;
            String format = b.bitLength() >= 32 && b.bitLength() < 64 ? "int64" : null;
            return scalar(key, "integer", b.toString(), null, format);
        }
        if (rawValue instanceof Number) {
            return scalar(key, "number", decimalText((Number) rawValue), null, null);
        }
        if (rawValue instanceof Date) {
            Date d = (Date) rawValue;
            boolean dateOnly = Math.floorMod(d.getTime(), MILLIS_PER_DAY) == 0;
            String example = dateOnly
                    ? d.toInstant().atOffset(ZoneOffset.UTC).toLocalDate().toString()
                    : DateTimeFormatter.ISO_INSTANT.format(d.toInstant());
            return scalar(key, "string", example, null, dateOnly ? "date" : "date-time");
        }
        if (rawValue instanceof List) {
            return array(key, (List<?>) rawValue);
        }
        /* String, Map (nested objects are handled by the source) and anything else: text. */
        String value = rawValue.toString().trim();
        return scalar(key, "string", value, value.length(), null);
    }

    private static Property scalar(String key, String type, String example, Integer minLength, String format) {
        return new Property(key, type, null, null, example, minLength, null, format, null, null, true, null);
    }

    private static Property array(String key, List<?> list) {
        boolean allInt = true;
        boolean allNumber = true;
        boolean allBoolean = true;
        boolean anyMap = false;
        int minLength = 1;
        List<String> texts = new ArrayList<>();
        for (Object o : list) {
            boolean isInt = o instanceof Integer || o instanceof Long || o instanceof BigInteger
                    || o instanceof Short || o instanceof Byte;
            allInt &= isInt;
            allNumber &= o instanceof Number;
            allBoolean &= o instanceof Boolean;
            anyMap |= o instanceof Map;
            String text = o == null ? "null" : (o instanceof Number && !isInt ? decimalText((Number) o) : o.toString().trim());
            texts.add(text);
            minLength = Math.max(minLength, text.length());
        }
        String items;
        Integer min = null;
        String example = "[" + String.join(", ", texts) + "]";
        if (list.isEmpty()) {
            items = "string";
            example = null;
        } else if (anyMap) {
            items = "object";
            example = null;
        } else if (allInt) {
            items = "integer";
        } else if (allNumber) {
            items = "number";
        } else if (allBoolean) {
            items = "boolean";
        } else {
            items = "string";
            min = minLength;
        }
        return new Property(key, "array", items, true, example, min, null, null, null, null, true, null);
    }

    private static String decimalText(Number n) {
        try {
            if (n instanceof BigDecimal) return ((BigDecimal) n).toPlainString();
            return new BigDecimal(n.toString()).toPlainString();
        } catch (NumberFormatException e) {
            return n.toString();
        }
    }
}
