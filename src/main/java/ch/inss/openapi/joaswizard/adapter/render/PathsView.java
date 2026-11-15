package ch.inss.openapi.joaswizard.adapter.render;

import ch.inss.openapi.joaswizard.domain.HttpMethod;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.domain.ResourceName;

import java.util.Set;

/**
 * Template model of the paths section, getters named as the template variables.
 */
public final class PathsView {
    private final ResourceName name;
    private final String resourceId;
    private final Set<HttpMethod> methods;

    public PathsView(ResourceDefinition resource) {
        this.name = resource.name();
        this.resourceId = resource.idField();
        this.methods = resource.methods();
    }

    public boolean isGet() {
        return methods.contains(HttpMethod.GET);
    }

    public boolean isPost() {
        return methods.contains(HttpMethod.POST);
    }

    public boolean isPut() {
        return methods.contains(HttpMethod.PUT);
    }

    public boolean isPatch() {
        return methods.contains(HttpMethod.PATCH);
    }

    public boolean isDelete() {
        return methods.contains(HttpMethod.DELETE);
    }

    public boolean isAllquery() {
        return !(!methods.contains(HttpMethod.GET) && !methods.contains(HttpMethod.POST));
    }

    /** Counts all methods, including QUERY and EMPTY (legacy behaviour). */
    public boolean isPathIdQuery() {
        return !(methods.size() == 1 && methods.contains(HttpMethod.POST));
    }

    public String getResource() {
        return name.value();
    }

    public String getCapResource() {
        return name.cap();
    }

    public String getLowerResource() {
        return name.lower();
    }

    public String getLowerResources() {
        return name.lowerPlural();
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getOpenCurlyBrace() {
        return "{";
    }

    public String getCloseCurlyBrace() {
        return "}";
    }
}
