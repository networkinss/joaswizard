package ch.inss.openapi.joaswizard.domain;

import ch.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * Name of a resource (object) with the spellings used by the templates.
 * The locale handling reproduces the former {@code InputParameter} getters exactly:
 * {@link #lowerPlural()} uses the default locale, all others {@link Locale#ROOT} or no case change.
 *
 * @param value the resource name as given, not null.
 */
public record ResourceName(String value) {

    public ResourceName {
        Objects.requireNonNull(value, "value");
    }

    /** @return lower case (locale ROOT) with first letter upper case, e.g. Pet. */
    public String cap() {
        return StringUtils.capitalize(value.toLowerCase(Locale.ROOT));
    }

    /** @return {@link #cap()} plus "s". */
    public String capPlural() {
        return cap() + "s";
    }

    /** @return lower case (locale ROOT). */
    public String lower() {
        return value.toLowerCase(Locale.ROOT);
    }

    /** @return lower case in the DEFAULT locale plus "s" (kept as is for byte-identical output). */
    public String lowerPlural() {
        return value.toLowerCase() + "s";
    }

    /** @return the name as given plus "s". */
    public String plural() {
        return value + "s";
    }
}
