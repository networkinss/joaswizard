package ch.inss.openapi.joaswizard.port;

import ch.inss.openapi.joaswizard.application.DiagnosticCollector;

/**
 * Stores a generated specification.
 */
public interface SpecWriter {

    /**
     * @param spec           the specification.
     * @param file           target file, null or empty for the default (openapi.yaml). Missing parent folders are created.
     * @param legacyRelative true: resolve the file below the current folder even if it is absolute (deprecated
     *                       behaviour); false: an absolute file is written as given.
     * @param c              collector, receives an error if writing failed.
     * @return the file as written (the default if none was given), null if writing failed.
     */
    String write(String spec, String file, boolean legacyRelative, DiagnosticCollector c);
}
