package ch.inss.openapi.joaswizard.domain;

import java.util.List;
import java.util.Set;

/**
 * A resource (object) with its methods and properties.
 *
 * @param name       resource name.
 * @param idField    name of the id field.
 * @param methods    methods to generate paths for.
 * @param properties properties, null if the source could not be read (see {@link #unreadable(ResourceName)}).
 * @param nested     true for a schema of a nested object: only rendered in components/schemas (no paths, not counted),
 *                   its name is the schema name as is.
 */
public record ResourceDefinition(ResourceName name, String idField, Set<HttpMethod> methods, List<Property> properties,
                                 boolean nested) {

    public ResourceDefinition(ResourceName name, String idField, Set<HttpMethod> methods, List<Property> properties) {
        this(name, idField, methods, properties, false);
    }

    /**
     * @param schemaName exact schema name.
     * @param properties properties.
     * @return a nested-only definition.
     */
    public static ResourceDefinition nestedSchema(String schemaName, List<Property> properties) {
        return new ResourceDefinition(new ResourceName(schemaName), null, Set.of(), properties, true);
    }

    /**
     * @param name resource name.
     * @return a definition without properties (null), e.g. an Excel sheet that failed with stopOnError.
     */
    public static ResourceDefinition unreadable(ResourceName name) {
        return new ResourceDefinition(name, null, Set.of(), null);
    }

    /** @return true if the properties could not be read. */
    public boolean isUnreadable() {
        return properties == null;
    }
}
