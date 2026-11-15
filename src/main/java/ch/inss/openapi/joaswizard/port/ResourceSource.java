package ch.inss.openapi.joaswizard.port;

import ch.inss.openapi.joaswizard.application.DiagnosticCollector;
import ch.inss.openapi.joaswizard.domain.GenerationOptions;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;

import java.util.List;

/**
 * Reads resource definitions from sample data.
 */
public interface ResourceSource {

    /** @return true if this source can read the input. */
    boolean supports(SourceInput input);

    /**
     * @param input   the input, must be supported.
     * @param options generation options.
     * @param c       collector for errors and warnings.
     * @return the resources in generation order, null if the input could not be read (reason reported to the
     * collector, or no data).
     */
    List<ResourceDefinition> read(SourceInput input, GenerationOptions options, DiagnosticCollector c);
}
