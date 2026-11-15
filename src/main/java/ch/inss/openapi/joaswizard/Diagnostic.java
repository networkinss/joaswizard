package ch.inss.openapi.joaswizard;

import java.util.Objects;

/**
 * One message produced while generating an OpenAPI specification.
 * Immutable.
 */
public final class Diagnostic {

    /** Severity of a diagnostic. */
    public enum Severity {
        /** The generation failed or its result is not reliable. */
        ERROR,
        /** The generation went on, but something should be checked. */
        WARNING
    }

    private final Severity severity;
    private final String message;

    /**
     * @param severity severity, must not be null.
     * @param message  message, null is replaced by an empty string.
     */
    public Diagnostic(Severity severity, String message) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.message = message == null ? "" : message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Diagnostic)) return false;
        Diagnostic that = (Diagnostic) o;
        return severity == that.severity && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, message);
    }

    @Override
    public String toString() {
        return severity + ": " + message;
    }
}
