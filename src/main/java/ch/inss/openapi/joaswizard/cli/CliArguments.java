package ch.inss.openapi.joaswizard.cli;

import ch.inss.openapi.joaswizard.InputParameter;

/** The six command line values, in argument order. */
public record CliArguments(String input, String output, String resource, String idField,
                           String sourcetype, String methods) {

    private static final String SEP = System.getProperty("file.separator");

    /** Takes values from args, asks for the missing ones in argument order. Output gets the "./" prefix. */
    public static CliArguments resolve(String[] args, Prompter p) {
        String input;
        if (args.length < 1) {
            input = p.argOrAsk(args, 0,
                    "Jo needs six parameters.",
                    "Usage: <inputfile> <outpufile> <resource> <Idfield> <sourcetype> <methods>",
                    "You can enter them now.",
                    "Please enter file name you want to use, or, If you want to use a string, enter that one.");
        } else {
            input = args[0];
        }

        String output = p.argOrAsk(args, 1,
                "Please enter the name of the outputfile or leave empty for the default 'openapi.yaml'.");
        if (output.isEmpty()) output = "openapi.yaml";
        output = "." + SEP + output;

        String resource = p.argOrAsk(args, 2, "Please enter the name of the resource (objectname). Like e.g. 'pet'.");
        String idField = p.argOrAsk(args, 3, "Please enter the name of the field which is the key (ID) field.");

        String sourcetypeList = String.valueOf(InputParameter.getSourcetypeList());
        boolean stFromArg = args.length >= 5;
        String sourcetype = stFromArg ? args[4] : null;
        if (!stFromArg) {
            p.info("Please enter the sourcetype " + sourcetypeList + ".");
            p.info("Default is yamlfile, if no value is provided.");
        }
        sourcetype = p.validated(sourcetype, stFromArg, InputParameter::isValidSourcetype, "yamlfile",
                "Argument " + sourcetype + " is not a valid sourcetype.",
                "Please choose one of " + sourcetypeList);

        String methodList = String.valueOf(InputParameter.getAvalableMethodList());
        boolean mFromArg = args.length >= 6;
        String methods = null;
        if (mFromArg) {
            methods = args[5].isEmpty() ? "crud" : args[5];
        } else {
            p.info("Please enter a list of methods " + methodList + ".");
        }
        methods = p.validated(methods, mFromArg, InputParameter::isValidMethod, "crud",
                "Argument " + methods + " is not a valid method.",
                "Please choose one of " + methodList + (mFromArg ? "" : "."));

        return new CliArguments(input, output, resource, idField, sourcetype, methods);
    }
}
