package ch.inss.openapi.joaswizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Result of one generation call of {@link Joaswizard#generate(InputParameter)} or
 * {@link Joaswizard#generateToFile(InputParameter)}.
 * Immutable and independent of any other call.
 */
public final class GenerationResult {

    private final String spec;
    private final List<Diagnostic> diagnostics;
    private final String outputFile;

    private GenerationResult(String spec, List<Diagnostic> diagnostics, String outputFile) {
        this.spec = spec;
        this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
        this.outputFile = outputFile;
    }

    /**
     * @param spec        the generated specification, null if generation failed.
     * @param diagnostics all diagnostics in the order of occurrence.
     * @return a result without output file.
     */
    public static GenerationResult of(String spec, List<Diagnostic> diagnostics) {
        return new GenerationResult(spec, diagnostics == null ? Collections.<Diagnostic>emptyList() : diagnostics, null);
    }

    /**
     * @param spec        the generated specification, null if generation failed.
     * @param diagnostics all diagnostics in the order of occurrence.
     * @param outputFile  the file the specification was written to, null if not written.
     * @return a result with output file.
     */
    public static GenerationResult of(String spec, List<Diagnostic> diagnostics, String outputFile) {
        return new GenerationResult(spec, diagnostics == null ? Collections.<Diagnostic>emptyList() : diagnostics, outputFile);
    }

    /**
     * @return the full OpenAPI document, null if the generation failed.
     */
    public String getSpec() {
        return spec;
    }

    /**
     * @return all diagnostics (errors and warnings) in the order of occurrence, unmodifiable.
     */
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * @return the diagnostics with severity ERROR.
     */
    public List<Diagnostic> getErrors() {
        return filter(Diagnostic.Severity.ERROR);
    }

    /**
     * @return the diagnostics with severity WARNING.
     */
    public List<Diagnostic> getWarnings() {
        return filter(Diagnostic.Severity.WARNING);
    }

    /**
     * @return true if a specification was generated and no error was reported.
     */
    public boolean isOk() {
        return spec != null && getErrors().isEmpty();
    }

    /**
     * @return the file the specification was written to, null if it was not written
     * (always null for {@link Joaswizard#generate(InputParameter)}).
     */
    public String getOutputFile() {
        return outputFile;
    }

    private List<Diagnostic> filter(Diagnostic.Severity severity) {
        return Collections.unmodifiableList(diagnostics.stream()
                .filter(d -> d.getSeverity() == severity).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return "GenerationResult{ok=" + isOk() + ", outputFile=" + outputFile + ", diagnostics=" + diagnostics + "}";
    }
}
