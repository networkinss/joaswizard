package ch.inss.openapi.joaswizard.application;

import ch.inss.openapi.joaswizard.domain.Property;

/**
 * Guesses the OAS type of a property from its sample value (Yaml/JSON input).
 */
public final class TypeInferrer {

    /**
     * @param key      property name.
     * @param rawValue sample value as read from Yaml, may be null.
     * @return the property with guessed type, example and length.
     */
    public Property infer(String key, Object rawValue) {
        String value = rawValue == null ? "" : rawValue.toString().trim();
        boolean isNumeric = true;
        boolean isArray = false;
        boolean isNumber = false;
        int minLength = 1;
        String type = null;
        if (value.startsWith("[") && value.endsWith("]")) {
            String arrValue = value.substring(1, value.length() - 1);
            String[] arr = arrValue.split(",");
            isArray = true;
            for (String a : arr) {
                isNumeric = isNumeric && Numbers.isNumber(a);
                if (isNumeric) {
                    isNumber = isNumber || a.contains(".");
                }
                if (a.length() > minLength) minLength = a.trim().length();
            }
        } else {
            isNumeric = isNumeric && Numbers.isNumber(value);
            if (isNumeric) {
                isNumber = isNumber || value.contains(".");
            }
        }
        if (isArray) {
            type = "array";
        } else if (isNumeric) {
            if (isNumber) type = "number";
            else type = "integer";
        } else {
            type = "string";
            minLength = value.trim().length();
        }

        Boolean array = null;
        String typeArray = null;
        Integer min = null;
        if (isArray) {
            array = true;
            if (isNumber) typeArray = "number";
            else if (isNumeric) typeArray = "integer";
            else {
                typeArray = "string";
                min = minLength;
            }
        }
        if (type.equals("string")) min = minLength;
        return new Property(key, type, typeArray, array, value, min, null, null, null, null, true, null);
    }
}
