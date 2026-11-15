package ch.inss.openapi.joaswizard.domain;

/**
 * Content of the OpenAPI info and server section, same fields as {@code OasInfo}.
 */
public record ApiInfo(String openapi, String version, String title, String description, String serverUrl,
                      String serverDescription, String contactName, String licenseUrl, String licenseName,
                      String contactEmail, String contactUrl) {

    /**
     * @param newTitle the new title.
     * @return a copy with another title.
     */
    public ApiInfo withTitle(String newTitle) {
        return new ApiInfo(openapi, version, newTitle, description, serverUrl, serverDescription, contactName,
                licenseUrl, licenseName, contactEmail, contactUrl);
    }
}
