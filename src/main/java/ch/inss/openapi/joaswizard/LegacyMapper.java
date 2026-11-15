package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.adapter.source.YamlSource;
import ch.inss.openapi.joaswizard.application.DiagnosticCollector;
import ch.inss.openapi.joaswizard.domain.ApiInfo;
import ch.inss.openapi.joaswizard.domain.EnumValue;
import ch.inss.openapi.joaswizard.domain.GenerationOptions;
import ch.inss.openapi.joaswizard.domain.HttpMethod;
import ch.inss.openapi.joaswizard.domain.Property;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.domain.ResourceName;
import ch.inss.openapi.joaswizard.port.SourceInput;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Maps the legacy public types ({@link InputParameter}, {@link OasInfo}, {@link PropertyData}) to the domain types.
 */
final class LegacyMapper {

    private LegacyMapper() {
    }

    static final String RESOURCE_MISSING = "Resource must be set. Define resouce (object name).";
    private static final String UNSUPPORTED_TYPE = "Source type is not set or not supported.";
    private static final String DEFAULT_OUTPUT_FILE = Constants.DEFAULT_OUTPUT_FILE;

    /** How the source of a call is chosen. */
    enum Kind {
        /** By source type: Excel (stream only if there is no file) or Yaml/JSON. */
        AUTO,
        YAML,
        EXCEL_FILE,
        EXCEL_STREAM
    }

    /** A validated call: where the data come from and the options. */
    record Request(SourceInput source, GenerationOptions options) {
    }

    /**
     * The single place of input validation: defaults and mutations of the input (output file, CRUD, Excel source
     * type, sample data read from a Yaml/JSON file), rules, and the mapping to the generator input.
     * Errors and warnings go to the collector.
     *
     * @param in the input, may be null.
     * @param kind how the source is chosen.
     * @param c collector.
     * @return the request, empty if the input is not valid (reason reported).
     */
    static Optional<Request> prepare(InputParameter in, Kind kind, DiagnosticCollector c) {
        if (in == null) {
            c.error("Input parameter is missing.");
            return Optional.empty();
        }
        if (kind == Kind.AUTO) {
            boolean excel = in.getSourceType() == InputParameter.Sourcetype.EXCEL
                    || (in.getSourceType() == null && in.getInputFile() == null && in.getInputStream() != null);
            if (excel) {
                kind = in.getInputFile() == null && in.getInputStream() != null ? Kind.EXCEL_STREAM : Kind.EXCEL_FILE;
            } else {
                kind = Kind.YAML;
            }
        }
        if (kind != Kind.YAML) in.setSourceType(InputParameter.Sourcetype.EXCEL);
        /* DEFAULT values */
        if (in.getOutputFile() == null || in.getOutputFile().equals("")) {
            in.setOutputFile(DEFAULT_OUTPUT_FILE);
        }
        if (in.getMethodList().isEmpty()) {
            in.addMethod(InputParameter.Method.CRUD);
            c.warning("No REST API methods defined, setting to default CRUD.");
        }
        List<String> problems = structuralProblems(in);
        problems.remove(UNSUPPORTED_TYPE);
        if (!problems.isEmpty()) {
            c.error(problems.get(0));
            return Optional.empty();
        }
        InputParameter.Sourcetype type = in.getSourceType();
        if (type == InputParameter.Sourcetype.YAMLFILE || type == InputParameter.Sourcetype.JSONFILE) {
            if (!Util.fileExists(in.getInputFile())) {
                c.error(formatName(in) + " file not found: " + in.getInputFile());
                return Optional.empty();
            }
            in.setSampleYamlData(YamlSource.readFile(in.getInputFile()));
            if (in.getSampleYamlData() == null) {
                c.error("No " + formatName(in) + " data provided.");
                return Optional.empty();
            }
        }
        SourceInput source;
        if (kind == Kind.EXCEL_STREAM) {
            source = new SourceInput.ExcelStream(in.getInputStream());
        } else if (kind == Kind.EXCEL_FILE) {
            source = new SourceInput.ExcelFile(in.getInputFile());
        } else {
            boolean json = type == InputParameter.Sourcetype.JSONFILE || type == InputParameter.Sourcetype.JSONSTRING;
            source = new SourceInput.YamlText(in.getSampleYamlData(), in.getInputFile(), json);
        }
        return Optional.of(new Request(source, toOptions(in)));
    }

    /**
     * The structural rules of an input, without defaults, file access or side effects.
     * Yaml and JSON source types follow the same rules.
     *
     * @param in the input.
     * @return the problems in the order of the rules, empty if valid. A source type that is not set or not supported
     * is a problem for {@link InputParameter#checkValid()}; {@link #prepare} lets it pass (as before).
     */
    static List<String> structuralProblems(InputParameter in) {
        List<String> problems = new ArrayList<>();
        InputParameter.Sourcetype type = in.getSourceType();
        if (type == InputParameter.Sourcetype.EXCEL) {
            boolean valid = (in.getInputFile() != null || in.getInputStream() != null) && in.getMethodList() != null;
            if (!valid) problems.add("Input parameter are not sufficient. Please define input file or input stream.");
        } else if (type == InputParameter.Sourcetype.YAMLFILE || type == InputParameter.Sourcetype.JSONFILE) {
            boolean valid = in.getInputFile() != null && in.getResource() != null && in.getResourceId() != null;
            if (!valid) problems.add("Input parameter for a " + formatName(in) + " file are not sufficient. Please define input file, resourceId and resource.");
        } else if (type == InputParameter.Sourcetype.YAMLSTRING || type == InputParameter.Sourcetype.JSONSTRING) {
            boolean valid = in.getSampleYamlData() != null && in.getResource() != null && in.getResourceId() != null;
            if (!valid) problems.add("Input parameter for a " + formatName(in) + " String are not sufficient. Please define sample data, resourceId and resource.");
        } else {
            problems.add(UNSUPPORTED_TYPE);
        }
        if (in.getResource() == null && type != InputParameter.Sourcetype.EXCEL) {
            problems.add(RESOURCE_MISSING);
        }
        return problems;
    }

    private static String formatName(InputParameter in) {
        InputParameter.Sourcetype type = in.getSourceType();
        return (type == InputParameter.Sourcetype.JSONFILE || type == InputParameter.Sourcetype.JSONSTRING) ? "JSON" : "Yaml";
    }

    /**
     * @param in legacy input, resource must not be null.
     * @return resource definition with properties from the schema data if they are a "data" list, else null.
     * @throws NullPointerException if the resource is null.
     */
    static ResourceDefinition toResource(InputParameter in) {
        return new ResourceDefinition(new ResourceName(in.getResource()), in.getResourceId(), toMethods(in), null);
    }

    private static Set<HttpMethod> toMethods(InputParameter in) {
        Set<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
        if (in.getMethodList() != null) {
            for (InputParameter.Method m : in.getMethodList()) {
                if (m == InputParameter.Method.CRUD) methods.addAll(HttpMethod.crud());
                else methods.add(HttpMethod.valueOf(m.name()));
            }
        }
        return methods;
    }

    /**
     * @param in legacy input.
     * @return the generation options of the input.
     */
    static GenerationOptions toOptions(InputParameter in) {
        Integer max = in.getMaxobjects();
        return new GenerationOptions(toMethods(in), in.getResource(), in.getResourceId(), in.isDoInfo(), in.isDoPaths(), in.isDoSchemas(),
                in.isStopOnError(), in.isDoDefaultSamples(), in.isPrefixMatch(), max == null ? 100 : max,
                in.getMappingFile(), in.getOasInfo() == null ? null : toApiInfo(in.getOasInfo()));
    }

    /**
     * Domain to legacy: one copy of the base input per resource, with the properties as schema data.
     * A definition without name keeps the resource of the base input, one without properties gets null schema data.
     *
     * @param definitions resources as read by a source.
     * @param base        the input the copies are made from.
     * @return the per-resource inputs.
     */
    static List<InputParameter> toInputParameters(List<ResourceDefinition> definitions, InputParameter base) {
        List<InputParameter> result = new ArrayList<>();
        for (ResourceDefinition def : definitions) {
            InputParameter ip = new InputParameter(base);
            if (def.name() != null) ip.setResource(def.name().value());
            if (def.properties() == null) {
                ip.setSchemaData(null);
            } else {
                List<PropertyData> list = new ArrayList<>();
                for (Property p : def.properties()) list.add(toPropertyData(p));
                HashMap<String, Object> data = new HashMap<>();
                data.put("data", list);
                ip.setSchemaData(data);
            }
            result.add(ip);
        }
        return result;
    }

    static PropertyData toPropertyData(Property p) {
        PropertyData d = new PropertyData(p.key(), p.type());
        d.setArray(p.array());
        d.setTypeArray(p.typeArray());
        d.setExamplevalue(p.example());
        d.setMinlength(p.minLength());
        d.setMaxLength(p.maxLength());
        d.setFormat(p.format());
        d.setPattern(p.pattern());
        d.setRequired(p.required());
        d.setDescription(p.description());
        if (p.enumValues() != null) {
            List<PropertyData.EnumItem> items = new ArrayList<>();
            for (EnumValue e : p.enumValues()) items.add(new PropertyData.EnumItem(e.value(), e.asString()));
            d.setEnumitems(items);
        }
        return d;
    }

    static ApiInfo toApiInfo(OasInfo o) {
        return new ApiInfo(o.getOpenapi(), o.getVersion(), o.getTitle(), o.getDescription(), o.getServerUrl(),
                o.getServerDescription(), o.getContactName(), o.getLicenseUrl(), o.getLicenseName(),
                o.getContactEmail(), o.getContactUrl());
    }

    static Property toProperty(PropertyData d) {
        List<EnumValue> enums = null;
        if (d.getEnumitems() != null) {
            enums = new ArrayList<>();
            for (PropertyData.EnumItem e : d.getEnumitems()) enums.add(new EnumValue(e.getValue(), e.getAsString()));
        }
        return new Property(d.getKey(), d.getType(), d.getTypeArray(), d.getArray(), d.getExamplevalue(),
                d.getMinlength(), d.getMaxLength(), d.getFormat(), d.getPattern(), enums, d.getRequired(),
                d.getDescription());
    }
}
