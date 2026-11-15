package ch.inss.openapi.joaswizard.adapter.render;

import ch.inss.openapi.joaswizard.domain.EnumValue;

/**
 * Template model of one enum value.
 */
public final class EnumItemView {
    private final EnumValue item;

    public EnumItemView(EnumValue item) {
        this.item = item;
    }

    public String getValue() {
        return item.value();
    }

    public boolean getAsString() {
        return item.asString();
    }
}
