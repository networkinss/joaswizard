package ch.inss.openapi.joaswizard.adapter.render;

import ch.inss.openapi.joaswizard.domain.ApiInfo;

/**
 * Template model of the info section, getters named as the template variables.
 */
public final class InfoView {
    private final ApiInfo info;

    public InfoView(ApiInfo info) {
        this.info = info;
    }

    public String getOpenapi() {
        return info.openapi();
    }

    public String getVersion() {
        return info.version();
    }

    public String getTitle() {
        return info.title();
    }

    public String getDescription() {
        return info.description();
    }

    public String getServerUrl() {
        return info.serverUrl();
    }

    public String getServerDescription() {
        return info.serverDescription();
    }

    public String getContactName() {
        return info.contactName();
    }

    public String getLicenseUrl() {
        return info.licenseUrl();
    }

    public String getLicenseName() {
        return info.licenseName();
    }

    public String getContactEmail() {
        return info.contactEmail();
    }

    public String getContactUrl() {
        return info.contactUrl();
    }
}
