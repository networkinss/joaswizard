package ch.inss.openapi.joaswizard;

import ch.inss.openapi.joaswizard.cli.CliArguments;
import ch.inss.openapi.joaswizard.cli.Prompter;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = null;
    /** Strong reference, so the configured package logger is not garbage collected. */
    private static Logger libraryLogger = null;
    private static final ConsoleHandler consoleHandler = new ConsoleHandler();

    static {
        consoleHandler.setLevel(Level.ALL);
        Formatter formatter = new LogFormatter();
        consoleHandler.setFormatter(formatter);
        libraryLogger = Logger.getLogger("ch.inss.openapi.joaswizard");
        libraryLogger.addHandler(consoleHandler);
        libraryLogger.setLevel(Level.INFO);
        libraryLogger.setUseParentHandlers(false);
        logger = Logger.getLogger(Main.class.getName());
    }

    /**
     * Create OAS3 document with command line arguments or input.
     *
     * @param args inputfile outpufile resource Idfield sourcetype methods
     */
    public static void main(String[] args) {
        int exitCode = run(args, System.in);
        boolean help = args.length == 1 && args[0].equalsIgnoreCase("help");
        if (help || exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /** Testable core of main. Returns the exit code main would use; a generation failure is not an error code. */
    static int run(String[] args, InputStream in) {
        logp("main", "Starting main.");
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            logp("main", "Jo needs six parameters.");
            logp("main", "Usage: <inputfile> <outpufile> <resource> <Idfield> <sourcetype> <methods>");
            logp("main", "You can enter them interactively if you omit some of them.");
            return 0;
        }
        CliArguments cli = CliArguments.resolve(args, new Prompter(new Scanner(new InputStreamReader(in))));

        InputParameter inputParameter = new InputParameter();
        if (cli.sourcetype().equalsIgnoreCase(InputParameter.Sourcetype.YAMLFILE.toString())
                || cli.sourcetype().equalsIgnoreCase(InputParameter.Sourcetype.EXCEL.toString())) {
            if (!new File(cli.input()).isFile()) {
                logp(Level.SEVERE, "main", "Could not find file " + cli.input() + ".");
                return 1;
            }
        }
        inputParameter.setInputFile(cli.input());
        inputParameter.setOutputFile(cli.output());
        inputParameter.setResource(cli.resource());
        inputParameter.setResourceId(cli.idField());
        inputParameter.setSourceType(cli.sourcetype());
        inputParameter.addMethods(cli.methods());

        Joaswizard joaswizard = new Joaswizard();
        if (cli.methods().equalsIgnoreCase("crud") && !inputParameter.getSourceType().equals(InputParameter.Sourcetype.EXCEL)) {
            inputParameter.addMethod(InputParameter.Method.CRUD);
        }
        GenerationResult result = joaswizard.generateToFile(inputParameter);
        /* Errors and warnings are logged (SEVERE/WARNING) while generating. No exit code here: tests call main. */
        if (result.getOutputFile() != null) {
            logp("main", "Created openapi file in " + inputParameter.getOutputFile());
        } else {
            logp("main", "An error occurred, could not create file ." + inputParameter.getOutputFile());
        }
        return 0;
    }

    public static void createOpenApiFromYamlfile(String[] args) {
        fromFileArgs(args, InputParameter.Sourcetype.YAMLFILE, "input.yaml", "createOpenApiFromYamlfile");
    }

    public static void createOpenApiFromJSONfile(String[] args) {
        fromFileArgs(args, InputParameter.Sourcetype.JSONFILE, "input.json", "createOpenApiFromJSONfile");
    }

    private static void fromFileArgs(String[] args, InputParameter.Sourcetype dflt, String inputLabel, String method) {
        logp(method, "Start to create OpenAPi from arguments.");
        if (args.length < 3) {
            logp(method, "Need four parameter.");
            logp(method, "Parameters are <" + inputLabel + "> <output.yaml> <objectname> <idfieldname> <sourcetype> <methods>");
            return;
        }
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(args[0]);
        inputParameter.setOutputFile(args[1]);
        inputParameter.setResource(args[2]);
        inputParameter.setResourceId(args.length >= 4 ? args[3] : "id");
        if (args.length >= 5) {
            inputParameter.setSourceType(args[4]);
        } else {
            inputParameter.setSourceType(dflt);
        }
        inputParameter.addMethod(InputParameter.Method.CRUD);
        new Joaswizard().generateToFile(inputParameter);
    }

    /** Logs with an explicit source method, so the console shows the original method names. */
    private static void logp(String method, String msg) {
        logp(Level.INFO, method, msg);
    }

    private static void logp(Level level, String method, String msg) {
        logger.logp(level, Main.class.getName(), method, msg);
    }

    public static void version() {
        System.out.println(Constants.version);
    }
}
