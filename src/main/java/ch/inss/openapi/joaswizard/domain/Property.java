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
 */
public record Property(String key, String type, String typeArray, Boolean array, String example,
                       Integer minLength, Integer maxLength, String format, String pattern,
                       List<EnumValue> enumValues, Boolean required, String description) {
}
