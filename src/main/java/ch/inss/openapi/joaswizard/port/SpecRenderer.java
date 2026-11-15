package ch.inss.openapi.joaswizard.port;

import ch.inss.openapi.joaswizard.domain.ApiInfo;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;

/**
 * Renders the parts of an OpenAPI document.
 */
public interface SpecRenderer {

    /**
     * @param info info and server content, title must be set.
     * @return the info section including the trailing "paths:" line.
     */
    String info(ApiInfo info);

    /**
     * @param resource resource with its methods.
     * @return the path entries of the resource, starting with a line break.
     */
    String paths(ResourceDefinition resource);

    /**
     * @return start of components schemas including the standard error model.
     */
    String componentsHeader();

    /**
     * @param resource resource with its properties.
     * @return the schema object of the resource.
     */
    String schema(ResourceDefinition resource);
}
