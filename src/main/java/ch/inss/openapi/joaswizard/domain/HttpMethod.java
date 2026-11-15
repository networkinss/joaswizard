package ch.inss.openapi.joaswizard.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * HTTP methods known to the generator, mirrors {@code InputParameter.Method} without the CRUD shortcut.
 */
public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE, QUERY, EMPTY;

    /**
     * @return the methods a CRUD shortcut expands to (GET, PUT, POST, DELETE), unmodifiable.
     */
    public static Set<HttpMethod> crud() {
        return Collections.unmodifiableSet(EnumSet.of(GET, PUT, POST, DELETE));
    }
}
