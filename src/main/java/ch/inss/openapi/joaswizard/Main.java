package ch.inss.openapi.joaswizard;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     *
     * @param args inputfile outpufile resource Idfield sourcetype methods
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
            logger.info("Jo needs six parameters.");
            logger.info("Usage: <inputfile> <outpufile> <resource> <Idfield> <sourcetype> <methods>");
            logger.info("You can enter them now.");
            logger.info("Please enter file name you want to use, or, If you want to use a string, enter that one.");
            input = scanner.nextLine();
        } else {
            input = args[0];
        }

        if (args.length < 2) {
            logger.info("Please enter the name of the outputfile or leave empty for the default 'openapi.yaml'.");
//            logger.info("File will be created in the folder 'output'.");
            output = scanner.nextLine();
        } else {
            output = args[1];
        }
        if (output.equals("")) output = "openapi.yaml";
        output = "." + sep + output;

        if (args.length < 3) {
            logger.info("Please enter the name of the resource (objectname). Like e.g. 'pet'.");
            resource = scanner.nextLine();
        } else {
            resource = args[2];
        }

        if (args.length < 4) {
            logger.info("Please enter the name of the field which is the key (ID) field.");
            idfield = scanner.nextLine();
        } else {
            idfield = args[3];
        }

        if (args.length < 5) {
            logger.info("Please enter the sourcetype " + InputParameter.getSourcetypeList() + ".");
            logger.info("Default is yamlfile, if no value is provided.");
            boolean again = true;
            while (again) {
                sourcetype = scanner.nextLine();
                if (sourcetype.equals("")) sourcetype = "yamlfile";
                if (!InputParameter.isValidSourcetype(sourcetype)) {
                    logger.warning("Please choose one of " + InputParameter.getSourcetypeList());
                } else again = false;
            }
        } else {
            sourcetype = args[4];
            boolean ok = InputParameter.isValidSourcetype(sourcetype);
            if (ok == false) logger.info("Argument " + sourcetype + " is not a valid sourcetype.");
            while (ok == false) {
                logger.warning("Please choose one of " + InputParameter.getSourcetypeList());
                sourcetype = scanner.nextLine();
                if (sourcetype.equals("")) sourcetype = "yamlfile";
                ok = InputParameter.isValidSourcetype(sourcetype);
            }
        }

        if (args.length < 6) {
            logger.info("Please enter a list of methods " + InputParameter.getAvalableMethodList() + ".");
            boolean again = true;
            while (again) {
                methods = scanner.nextLine();
                if (methods.equals("")) methods = "crud";
                if (!InputParameter.isValidMethod(methods)) {
                    logger.warning("Please choose one of " + InputParameter.getAvalableMethodList() + ".");
                } else again = false;
            }
        } else {
            methods = args[5];
            if (methods.equals("")) methods = "crud";
            boolean ok = InputParameter.isValidMethod(methods);
            if (ok == false) logger.info("Argument " + methods + " is not a valid method.");
            while (ok == false) {
                logger.warning("Please choose one of " + InputParameter.getAvalableMethodList());
                methods = scanner.nextLine();
                if (methods.equals("")) methods = "crud";
                ok = InputParameter.isValidMethod(methods);
            }
        }

        InputParameter inputParameter = new InputParameter();
        if (sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.YAMLFILE.toString()) || sourcetype.equalsIgnoreCase(InputParameter.Sourcetype.EXCEL.toString())) {
            File file = new File(input);
            if (file.isFile() == false) {
                logger.severe("Could not find file " + input + ".");
                System.exit(1);
            }
        }
        inputParameter.setInputFile(input);
        inputParameter.setOutputFile(output);
        inputParameter.setResource(resource);
        inputParameter.setResourceId(idfield);
        inputParameter.setSourceType(sourcetype);
        inputParameter.addMethods(methods);

        Joaswizard joaswizard = new Joaswizard();
        boolean ok = false;
        if (methods.equalsIgnoreCase("crud") && inputParameter.getSourceType().equals(InputParameter.Sourcetype.EXCEL) == false) {
            ok = joaswizard.createCrudFromYamlToFile(inputParameter);
        } else if (inputParameter.getSourceType().toString().equalsIgnoreCase(InputParameter.Sourcetype.EXCEL.toString())) {
            ok = joaswizard.createFromExcelToFile(inputParameter);
        } else {
            ok = joaswizard.createFromYamlToFile(inputParameter);
        }
        if(ok)  {
            logger.info("Created openapi file in " + inputParameter.getOutputFile());
        }else{
            logger.info("An error occurred, could not create file ." + inputParameter.getOutputFile());
        }
    }

    public static void createOpenApiFromYamlfile(String[] args) {
        logger.info("Start to create OpenAPi from arguments.");
        if (args.length < 3) {
            logger.info("Need four parameter.");
            logger.info(          "Parameters are <input.yaml> <output.yaml> <objectname> <idfieldname> <sourcetype> <methods>");
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
        joaswizard.createCrudFromYamlToFile(inputParameter);
    }
}
