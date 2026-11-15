package ch.inss.openapi.joaswizard.adapter.render;

import ch.inss.openapi.joaswizard.domain.EnumValue;
import ch.inss.openapi.joaswizard.domain.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Template model of one schema property. Getter names and types match the former PropertyData,
 * so Handlebars truthiness (null Boolean, 0, empty string) is unchanged.
 */
public final class PropertyView {
    private final Property p;

    public PropertyView(Property p) {
        this.p = p;
    }

    public String getKey() {
        return p.key();
    }

    public String getLowKey() {
        return p.key().toLowerCase();
    }

    public String getType() {
        return p.type();
    }

    public Boolean getArray() {
        return p.array();
    }

    public String getTypeArray() {
        return p.typeArray();
    }

    public String getFormat() {
        return p.format();
    }

    public String getPattern() {
        return p.pattern();
    }

    public Integer getMinlength() {
        return p.minLength();
    }

    public Integer getMaxLength() {
        return p.maxLength();
    }

    public Boolean getRequired() {
        return p.required();
    }

    public String getDescription() {
        return p.description();
    }

    public String getExamplevalue() {
        return p.example();
    }

    public List<EnumItemView> getEnumitems() {
        List<EnumValue> values = p.enumValues();
        if (values == null) return null;
        List<EnumItemView> result = new ArrayList<>(values.size());
        for (EnumValue v : values) result.add(new EnumItemView(v));
        return result;
    }

    /**
     * A list (rendered as flow sequence) for array properties, otherwise the text.
     */
    public Object getExample() {
        String example = p.example();
        if (Boolean.TRUE.equals(p.array()) && example != null
                && example.startsWith("[") && example.endsWith("]")) {
            String inner = example.substring(1, example.length() - 1);
            List<String> items = new ArrayList<>();
            for (String item : inner.split(",")) {
                if (!item.trim().isEmpty()) items.add(item.trim());
            }
            return items;
        }
        return example;
    }
}
