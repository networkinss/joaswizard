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
 */
public record ResourceDefinition(ResourceName name, String idField, Set<HttpMethod> methods, List<Property> properties) {

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
