package ch.inss.openapi.joaswizard.application;

/**
 * Number detection for sample values.
 */
public final class Numbers {

    private Numbers() {
    }

    /**
     * Optional leading minus, digits, optional single decimal part. null or blank = false.
     *
     * @param str text to check.
     * @return true if the trimmed text is a number.
     */
    public static boolean isNumber(String str) {
        return str != null && str.trim().matches("-?[0-9]+(\\.[0-9]+)?");
    }
}
