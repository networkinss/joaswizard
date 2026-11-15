package ch.inss.openapi.joaswizard.domain;

import java.util.Set;

/**
 * Options of one generation run.
 *
 * @param methods          requested methods.
 * @param resource         resource name of the caller, used for a source document that is one unnamed resource
 *                         (flat Yaml); null if not needed.
 * @param resourceId       name of the id field.
 * @param doInfo           render the info section.
 * @param doPaths          render the paths.
 * @param doSchemas        render the schemas.
 * @param stopOnError      stop on the first unmappable type (Excel).
 * @param doDefaultSamples create default samples.
 * @param prefixMatch      prefix match of DB types.
 * @param maxObjects       maximum number of objects for schemas.
 * @param mappingFile      custom mapping file or null.
 * @param info             info section content.
 */
public record GenerationOptions(Set<HttpMethod> methods, String resource, String resourceId, boolean doInfo, boolean doPaths,
                                boolean doSchemas, boolean stopOnError, boolean doDefaultSamples,
                                boolean prefixMatch, int maxObjects, String mappingFile, ApiInfo info) {
}
