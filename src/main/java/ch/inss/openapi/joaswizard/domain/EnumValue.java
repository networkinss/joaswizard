package ch.inss.openapi.joaswizard.domain;

/**
 * One enum value of a property.
 *
 * @param value    the value as text.
 * @param asString true if number-like values must be quoted (string typed property).
 */
public record EnumValue(String value, boolean asString) {
}
