package ch.inss.joaswizard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.logging.*;

import static java.lang.System.currentTimeMillis;

public class Main implements Constants {
    private static Logger logger = null;
    public static ConsoleHandler consoleHandler = new ConsoleHandler();
    static {
        
        consoleHandler.setLevel(Level.ALL);
        Formatter formatter = new LogFormatter();
        consoleHandler.setFormatter(formatter);
        logger = Logger.getLogger(Main.class.getName());
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
    }

    /**
     * Create OAS3 document with command line arguments or input.
     */
    public static void main(String[] args) {
        logger.info("Starting main.");
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        String input = null;
        String output = null;
        String resource = null;
        String idfield = null;
        String sourcetype = null;
        String methods = null;
        if (args.length < 1) {
            System.out.println("Jo needs six parameters.");
            System.out.println("Usage: <inputfile> <outpufile> <resource> <Idfield> <sourcetype> <methods>");
            System.out.println("You can enter them now.");
            System.out.println("Please enter file name you want to use, or, If you want to use a string, enter that one.");
            input = scanner.nextLine();
        } else {
            input = args[0];
        }

        if (args.length < 2) {
            System.out.println("Please enter the name of the outputfile or leave empty for the default 'openapi.yaml'.");
//            System.out.println("File will be created in the folder 'output'.");
            output = scanner.nextLine();
        } else {
            output = args[1];
        }
        if (output.equals("")) output = "openapi.yaml";
        output = ".." + sep + output;

        if (args.length < 3) {
            System.out.println("Please enter the name of the resource (objectname). Like e.g. 'pet'.");
            resource = scanner.nextLine();
        } else {
            resource = args[2];
        }

        if (args.length < 4) {
            System.out.println("Please enter the name of the field which is the key (ID) field.");
            idfield = scanner.nextLine();
        } else {
            idfield = args[3];
        }

        if (args.length < 5) {
            System.out.println("Please enter the sourcetype " + InputParameter.getSourcetypeList() + ".");
            System.out.println("Default is yamlfile, if no value is provided.");
            boolean again = true;
            while (again) {
                sourcetype = scanner.nextLine();
                if (sourcetype.equals("")) sourcetype = "yamlfile";
                if (!InputParameter.isValidSourcetype(sourcetype)) {
                    System.out.println("Please choose one of " + InputParameter.getSourcetypeList());
                } else again = false;
            }
        } else {
            sourcetype = args[4];
            boolean ok = InputParameter.isValidSourcetype(sourcetype);
            if (ok == false) System.out.println("Argument " + sourcetype + " is not a valid sourcetype.");
            while (ok == false) {
                System.out.println("Please choose one of " + InputParameter.getSourcetypeList());
                sourcetype = scanner.nextLine();
                if (sourcetype.equals("")) sourcetype = "yamlfile";
                ok = InputParameter.isValidSourcetype(sourcetype);
            }
        }

        if (args.length < 6) {
            System.out.println("Please enter a list of methods " + InputParameter.getAvalableMethodList() + ".");
            boolean again = true;
            while (again) {
                methods = scanner.nextLine();
                if (methods.equals("")) methods = "crud";
                if (!InputParameter.isValidMethod(methods)) {
                    System.out.println("Please choose one of " + InputParameter.getAvalableMethodList() + ".");
                } else again = false;
            }
        } else {
            methods = args[5];
            if (methods.equals("")) methods = "crud";
            boolean ok = InputParameter.isValidMethod(methods);
            if (ok == false) System.out.println("Argument " + methods + " is not a valid method.");
            while (ok == false) {
                System.out.println("Please choose one of " + InputParameter.getAvalableMethodList());
                methods = scanner.nextLine();
                if (methods.equals("")) methods = "crud";
                ok = InputParameter.isValidMethod(methods);
            }
        }

        InputParameter inputParameter = new InputParameter();
        if (sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.YAMLFILE.toString()) || sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.EXCEL.toString())) {
            File file = new File(input);
            if (file.isFile() == false) {
                System.out.println("Could not find file " + sourcetype + ".");
                System.exit(1);
            }
        }
        inputParameter.setInputFile(input);
        inputParameter.setOutputFile(output);
        inputParameter.setResource(resource);
        inputParameter.setResourceId(idfield);
        inputParameter.setSourceType(sourcetype);
        inputParameter.addMethod(methods);

        Joaswizard joaswizard = new Joaswizard();
        if (methods.equalsIgnoreCase("crud") && inputParameter.getSourceType().equals(InputParameter.Sourcetype.EXCEL) == false) {
            joaswizard.createCrudFile(inputParameter);
        } else if (inputParameter.getSourceType().toString().equalsIgnoreCase(InputParameter.Sourcetype.EXCEL.toString())) {
            joaswizard.createFromExcel(inputParameter);
        } else {
            joaswizard.createMethodsFromSingleYamlObject(inputParameter);
        }
        logger.info("Created openapi file in output/" + inputParameter.getOutputFile());
    }

    public static void createOpenApiFromYamlfile(String[] args) {
        System.out.println("Start to create OpenAPi from arguments.");
        if (args.length < 3) {
            System.out.println("Need four parameter.");
            System.out.println("Usage: <inputfile> <outpufile> <resource> <Idfield>");
            System.exit(1);
        }
        InputParameter inputParameter = new InputParameter();
        inputParameter.setInputFile(args[0]);
        inputParameter.setOutputFile(args[1]);
        inputParameter.setResource(args[2]);
        if (args.length >= 4) {
            inputParameter.setResourceId(args[3]);
        } else {
            inputParameter.setResourceId("id");
        }

        if (args.length >= 5) {
            inputParameter.setSourceType(args[4]);
        } else {
            inputParameter.setSourceType(InputParameter.Sourcetype.YAMLFILE);
        }
        Joaswizard joaswizard = new Joaswizard();
        joaswizard.createCrudFile(inputParameter);
    }
}
