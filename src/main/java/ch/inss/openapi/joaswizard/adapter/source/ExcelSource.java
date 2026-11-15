package ch.inss.openapi.joaswizard.adapter.source;

import ch.inss.openapi.joaswizard.Constants;
import ch.inss.openapi.joaswizard.Header;
import ch.inss.openapi.joaswizard.application.DiagnosticCollector;
import ch.inss.openapi.joaswizard.application.Numbers;
import ch.inss.openapi.joaswizard.application.TypeMapper;
import ch.inss.openapi.joaswizard.application.TypeMapper.MappedType;
import ch.inss.openapi.joaswizard.domain.EnumValue;
import ch.inss.openapi.joaswizard.domain.GenerationOptions;
import ch.inss.openapi.joaswizard.domain.Property;
import ch.inss.openapi.joaswizard.domain.ResourceDefinition;
import ch.inss.openapi.joaswizard.domain.ResourceName;
import ch.inss.openapi.joaswizard.port.ResourceSource;
import ch.inss.openapi.joaswizard.port.SourceInput;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Reads resources from an Excel workbook, one sheet per resource. Types are explicit.
 * OAS type, format and pattern are defined in this sequence:
 * 1. Read values from Excel in columns with name: OasType, OasFormat, OasPattern.
 * 2. If not defined there, it will check the mapping of the DbType.
 * 3. If no mapping is defined, it will guess. If it can be parsed to a number, it will be type integer. All other is a string.
 */
public final class ExcelSource implements ResourceSource, Constants {

    private static final Logger logger = Logger.getLogger(ExcelSource.class.getName());

    @Override
    public boolean supports(SourceInput input) {
        return input instanceof SourceInput.ExcelFile || input instanceof SourceInput.ExcelStream;
    }

    @Override
    public List<ResourceDefinition> read(SourceInput input, GenerationOptions options, DiagnosticCollector c) {
        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        HashMap<String, List<Map<String, String>>> sheets;
        if (input instanceof SourceInput.ExcelFile f) {
            sheets = reader.readExcelfile(f.path());
            if (sheets == null) {
                c.error("Could not read Excel file " + f.path() + ".");
                return null;
            }
        } else {
            sheets = reader.readExcelStream(((SourceInput.ExcelStream) input).stream());
            if (sheets == null) {
                c.error("Could not read Excel input stream.");
                return null;
            }
        }
        List<ResourceDefinition> result = new ArrayList<>();
        for (String sheetName : sheets.keySet()) {
            ResourceName name = new ResourceName(sheetName);
            List<Property> properties = readSheet(sheetName, sheets.get(sheetName), options, c);
            result.add(properties == null ? ResourceDefinition.unreadable(name)
                    : new ResourceDefinition(name, options.resourceId(), options.methods(), properties));
        }
        return result;
    }

    /** @return the properties of the sheet, null if stopOnError stopped reading it. */
    private List<Property> readSheet(String sheetName, List<Map<String, String>> mapList, GenerationOptions options, DiagnosticCollector c) {
        List<Property> list = new ArrayList<>();

        /* If not in OasType field defined, it will try to take the type from the column DbType. */
        Map<String, MappedType> mapping = MappingFileLoader.load(options.mappingFile() == null ? DEFAULT_MAPPING : options.mappingFile());
        if (mapping == null) {
            mapping = new CaseInsensitiveMap<>();
        }
        TypeMapper typeMapper = new TypeMapper(mapping, options.prefixMatch());
        String logPrefix = "Sheet " + sheetName + ": ";
        int idx = 0;
        int countEmpty = 0;
        checkHeaderValidity(mapList.get(0), sheetName, c);
        /* Define each property of one object. */
        for (Map<String, String> sheetMap : mapList) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            CaseInsensitiveMap<String, String> sheetCIMap = new CaseInsensitiveMap(sheetMap);
            idx++;

            String key = sheetCIMap.get(Header.NAME);
            String sampleValue = sheetCIMap.get(Header.OASEXAMPLE);
            String type = null;
            if (sheetCIMap.containsKey(Header.OASTYPE)) type = sheetCIMap.get(Header.OASTYPE);
            if ((key == null || key.equals("")) && (sampleValue == null || sampleValue.equals("") && (type == null || type.equals("")))) {
                logger.fine(logPrefix + "Empty row or not enough data to build an OAS3 schema object (at line: " + idx + ").");
                countEmpty++;
                if (countEmpty > 2) {
                    logger.fine(logPrefix + "Found 3 or more empty lines beginning row " + (idx - 1) + ", assuming no more data and stop reading lines here..");
                    break;
                }
                continue;
            }
            if (key == null || key.equals("")) {
                key = UNDEFINED;
                c.warning(logPrefix + "Excel header '" + Header.NAME + "' is missing'. Name of object will be 'undefined'.");
            } else key = key.trim();
            logger.fine(logPrefix + "Line " + idx + ", key: " + key);

            MappedType mapped = MappedType.EMPTY;
            /* Define type (OasType). */
            if (type == null || type.equals("")) {
                /* DB type assignment if Oastype is not defined but DBType is present. */
                String dbtype = sheetCIMap.get(Header.DBTYPE);
                if (dbtype != null && "".equals(dbtype) == false) {
                    mapped = typeMapper.map(dbtype);
                    type = mapped.type();
                }
            }
            if ((type == null || type.equals(""))) {
                if (options.stopOnError()) {
                    c.error("Type mapping error in line " + idx + ", key: " + key);
                    c.error("Type cannot be mapped or is not valid. Breaking because stopOnError is set to true.");
                    return null;
                }
                if (sampleValue != null) {
                    type = (Numbers.isNumber(sampleValue) ? "integer" : "string");
                } else {
                    type = "string";
                }
            } else if (type != null) {
                type = type.trim().toLowerCase();
                if (Arrays.asList(DATATYPELIST).contains(type) == false) {
                    c.warning(logPrefix + "row " + (idx + 1) + ", property '" + key + "': type '" + type + "' is not valid, using string.");
                    type = "string";
                }
            }

            /* Define example values (OasExample) and guess if not defined. */
            if ((sampleValue == null || "".equals(sampleValue))) {
                if (options.doDefaultSamples()) {
                    if (type.equals("string")) sampleValue = "string";
                    else if (type.equals("integer")) sampleValue = "1";
                    else if (type.equals("number")) sampleValue = "1.0";
                }
            } else {
                sampleValue = sampleValue.trim();
            }
            String example = null;
            if (sampleValue != null && !sampleValue.equals("")) example = sampleValue;

            /* Define formatting (OasFormat). */
            String outFormat = null;
            String format = sheetCIMap.get(Header.OASFORMAT);
            if (format == null || "".equals(format)) {
                /* Try to get mapped format from mapping.json if the DbType had been defined. */
                format = mapped.format();
            }
            if (format != null) {
                if (((type.equalsIgnoreCase("number") || type.equalsIgnoreCase("integer")) && format.equalsIgnoreCase("string"))
                        || (type.equalsIgnoreCase("number") && (format.equalsIgnoreCase("int32") || format.equalsIgnoreCase("int64")))
                        || (type.equalsIgnoreCase("integer") && (format.equalsIgnoreCase("float") || format.equalsIgnoreCase("double")))) {
                    c.warning(logPrefix + "Check if type and format fit together for dataline " + idx + ". Type: " + type + ", format: " + format);
                } else outFormat = format.trim();
            }

            Integer minLength = null;
            if (sheetCIMap.containsKey(Header.OASMIN)) {
                if (Numbers.isNumber(sheetCIMap.get(Header.OASMIN))) {
                    minLength = Integer.parseInt(sheetCIMap.get(Header.OASMIN));
                }
            }
            String description = null;
            String oasDescription = sheetCIMap.get(Header.OASDESCRIPTION);
            if (oasDescription != null && oasDescription.isEmpty() == false && oasDescription.isBlank() == false) {
                description = oasDescription;
            }

            String pattern = sheetCIMap.get(Header.OASPATTERN.toString());
            /* Older mappings contained the pattern already wrapped in apostrophes (raw insertion). */
            if (pattern != null && pattern.length() > 2 && pattern.startsWith("'") && pattern.endsWith("'")) {
                pattern = pattern.substring(1, pattern.length() - 1);
            }

            Boolean required;
            if (sheetCIMap.containsKey(Header.OASREQUIRED)) {
                required = Boolean.parseBoolean(sheetCIMap.get(Header.OASREQUIRED));
            } else if (sheetCIMap.containsKey(Header.DBNULLABLE)) {
                required = !Boolean.parseBoolean(sheetCIMap.get(Header.DBNULLABLE));
            } else {
                required = true;
            }

            List<EnumValue> enumValues = getOasEnum(sheetCIMap.get(Header.OASENUM), type);

            Integer maxLength = null;
            String max = sheetCIMap.get(Header.OASMAX);
            if (Numbers.isNumber(max)) {
                maxLength = Integer.parseInt(max);
            } else if (max != null && max.equals("") == false) {
                c.warning(logPrefix + "Value for maxLength is not a number: " + max);
            }
            list.add(new Property(key, type, null, null, example, minLength, maxLength, outFormat, pattern,
                    enumValues, required, description));
        }
        return list;
    }

    private void checkHeaderValidity(Map<String, String> map, String sheetName, DiagnosticCollector c) {
        if (map == null) return;
        for (String key : map.keySet()) {
            if (key == null || key.isEmpty()) continue;
            try {
                Header.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException iae) {
                c.warning("Unknown Excel header: '" + key + "' in sheet " + sheetName + ".");
            }
        }
    }

    private static List<EnumValue> getOasEnum(String e, String type) {
        if (e == null || e.equals("")) return null;
        boolean string = type != null && type.equalsIgnoreCase("string");
        List<EnumValue> items = new ArrayList<>();
        for (String item : e.split(",")) {
            items.add(new EnumValue(item.trim(), string));
        }
        return items;
    }
}
