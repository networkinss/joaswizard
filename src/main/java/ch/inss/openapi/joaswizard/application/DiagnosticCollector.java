package ch.inss.openapi.joaswizard.application;

import ch.inss.openapi.joaswizard.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collects the diagnostics of one call. Never shared between calls.
 * Messages are also logged under the logger {@code ch.inss.openapi.joaswizard.Joaswizard}, with the source
 * {@code Joaswizard$Collector.error/warning} as before, so the CLI console output stays the same.
 */
public final class DiagnosticCollector {

    private static final String LOGGER_NAME = "ch.inss.openapi.joaswizard.Joaswizard";
    private static final String SOURCE_CLASS = "ch.inss.openapi.joaswizard.Joaswizard$Collector";
    private static final Logger logger = Logger.getLogger(LOGGER_NAME);

    private final List<Diagnostic> list = new ArrayList<>();

    public void error(String message) {
        logger.logp(Level.SEVERE, SOURCE_CLASS, "error", message);
        list.add(new Diagnostic(Diagnostic.Severity.ERROR, message));
    }

    public void warning(String message) {
        logger.logp(Level.WARNING, SOURCE_CLASS, "warning", message);
        list.add(new Diagnostic(Diagnostic.Severity.WARNING, message));
    }

    public boolean hasErrors() {
        return list.stream().anyMatch(d -> d.getSeverity() == Diagnostic.Severity.ERROR);
    }

    /** @return the message of the last diagnostic with this severity, null if there is none. */
    public String last(Diagnostic.Severity severity) {
        String result = null;
        for (Diagnostic d : list) {
            if (d.getSeverity() == severity) result = d.getMessage();
        }
        return result;
    }

    /** @return all diagnostics in the order they occurred (read-only view). */
    public List<Diagnostic> list() {
        return Collections.unmodifiableList(list);
    }
}
