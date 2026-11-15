package ch.inss.openapi.joaswizard.domain;

/**
 * Small string helper, replaces the former vendored commons-lang3 copy.
 */
public final class Strings {

    private Strings() {
    }

    /**
     * Title-cases the first code point, the rest is unchanged (same semantics as commons-lang3 {@code StringUtils.capitalize}).
     *
     * @param str the string, may be null.
     * @return the capitalized string, null for null, "" for "".
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int first = str.codePointAt(0);
        int title = Character.toTitleCase(first);
        if (first == title) {
            return str;
        }
        return new StringBuilder(str.length()).appendCodePoint(title)
                .append(str, Character.charCount(first), str.length()).toString();
    }
}
