package ch.inss.openapi.joaswizard.domain;

import java.util.List;

/**
 * One property of a resource schema. Fields may be null if not defined (same semantics as {@code PropertyData}).
 *
 * @param key        property name.
 * @param type       OAS type.
 * @param typeArray  OAS type of the array items, if array.
 * @param array      true if the property is an array.
 * @param example    example value as text.
 * @param minLength  minimum length.
 * @param maxLength  maximum length.
 * @param format     OAS format.
 * @param pattern    OAS pattern.
 * @param enumValues enum values or null.
 * @param required   required flag.
 * @param description description.
 * @param ref        schema reference ({@code #/components/schemas/X} or an explicit one) if the property is an object, else null.
 * @param itemsRef   schema reference of the array items if an array of objects, else null.
 */
public record Property(String key, String type, String typeArray, Boolean array, String example,
                       Integer minLength, Integer maxLength, String format, String pattern,
                       List<EnumValue> enumValues, Boolean required, String description, String ref, String itemsRef) {

    /** Property without schema references (all but nested objects). */
    public Property(String key, String type, String typeArray, Boolean array, String example,
                    Integer minLength, Integer maxLength, String format, String pattern,
                    List<EnumValue> enumValues, Boolean required, String description) {
        this(key, type, typeArray, array, example, minLength, maxLength, format, pattern, enumValues, required,
                description, null, null);
    }
}
