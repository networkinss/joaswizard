package ch.inss.openapi.joaswizard;

import com.github.jknack.handlebars.Handlebars;

/**
 * Delegating shim, the implementation lives in {@link ch.inss.openapi.joaswizard.adapter.render.YamlScalar}.
 */
final class YamlScalar {

    private YamlScalar() {
    }

    static Handlebars newHandlebars() {
        return ch.inss.openapi.joaswizard.adapter.render.YamlScalar.newHandlebars();
    }

    static String quoteAsString(String s) {
        return ch.inss.openapi.joaswizard.adapter.render.YamlScalar.quoteAsString(s);
    }

    static String quoteIfNeeded(String s) {
        return ch.inss.openapi.joaswizard.adapter.render.YamlScalar.quoteIfNeeded(s);
    }

    static String quoteFlowItem(String s) {
        return ch.inss.openapi.joaswizard.adapter.render.YamlScalar.quoteFlowItem(s);
    }
}
