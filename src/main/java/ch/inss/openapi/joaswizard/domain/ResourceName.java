package ch.inss.openapi.joaswizard.domain;


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
        return Strings.capitalize(value.toLowerCase(Locale.ROOT));
    }

    /** @return {@link #cap()} pluralised. */
    public String capPlural() {
        return pluralize(cap());
    }

    /** @return lower case (locale ROOT). */
    public String lower() {
        return value.toLowerCase(Locale.ROOT);
    }

    /** @return lower case in the DEFAULT locale pluralised (kept as is for byte-identical output). */
    public String lowerPlural() {
        return pluralize(value.toLowerCase());
    }

    /** @return the name as given, pluralised. */
    public String plural() {
        return pluralize(value);
    }

    /**
     * Basic English plural, decided on the lower-cased form, the casing of the stem is kept:
     * consonant + y gives ies, s, x, z, ch, sh give es, everything else gets s.
     */
    private static String pluralize(String word) {
        String l = word.toLowerCase(Locale.ROOT);
        boolean caps = word.equals(word.toUpperCase(Locale.ROOT)) && !word.equals(l);
        int n = l.length();
        if (n > 1 && l.charAt(n - 1) == 'y' && "aeiou".indexOf(l.charAt(n - 2)) < 0) {
            return word.substring(0, n - 1) + (caps ? "IES" : "ies");
        }
        if (l.endsWith("s") || l.endsWith("x") || l.endsWith("z") || l.endsWith("ch") || l.endsWith("sh")) {
            return word + (caps ? "ES" : "es");
        }
        return word + (caps ? "S" : "s");
    }
}
