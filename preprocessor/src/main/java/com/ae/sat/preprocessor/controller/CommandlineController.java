package com.ae.sat.preprocessor.controller;

import com.ae.sat.preprocessor.common.model.formulas.Cnf;
import com.ae.sat.preprocessor.common.model.formulas.SolverAssignments;
import com.ae.sat.preprocessor.service.MinisatPreprocessService;
import com.ae.sat.preprocessor.service.SolverAssignmentService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by ae on 30-12-16.
 */

@Controller
public class CommandlineController implements CommandLineRunner {

    private static String NSOLVERS = "nsolvers";
    private static String HELP = "help";
    private static String CNF_FILE = "cnfFile";
    private static String PREPROCESS = "preprocess";
    private static String CNF_OUTPUT_FILE = "cnfOutputFile";
    private static String ASSUMPTION_OUTPUT_FILE = "assumptionOutputFile";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Options options;

    private CommandLineParser parser;

    private HelpFormatter formatter;

    @Autowired
    private SolverAssignmentService solverAssignmentService;

    @Autowired
    private MinisatPreprocessService minisatPreprocessService;

    /**
     * Initiates the sub-command:
     * Bas: Generously stolen from
     * http://www.amertkara.com/2016/03/05/spring-boot-multiple-command-line-runners.html
     */
    @PostConstruct
    public void init () {
        options = new Options();

        parser = new DefaultParser();

        formatter = new HelpFormatter();

        Option cnfFile = Option.builder(CNF_FILE)
                .desc("the location of the cnf file to be solved.")
                .hasArg()
                .argName(CNF_FILE)
                .build();

        Option help = Option.builder(HELP)
                .desc("Print this message and quit.")
                .build();

        Option amountOfSolver = Option.builder(NSOLVERS)
                .desc("Amount of solvers to be used.")
                .hasArg()
                .argName(NSOLVERS)
                .build();

        Option preprocess = Option.builder(PREPROCESS)
                .desc("Optional flag, if provided do minisat style preprocessing on formula.")
                .build();


        Option cnfOutputFile = Option.builder(CNF_OUTPUT_FILE)
                .desc("If preprocessing is done, the resulting cnf will be written to the provided path. " +
                      "Otherwise a temp file will be created.")
                .hasArg()
                .argName(CNF_OUTPUT_FILE)
                .build();

        Option assumptionOutputFile = Option.builder(ASSUMPTION_OUTPUT_FILE)
                .desc("The resulting assumption will be written to the provided path. " +
                        "Otherwise a temp file will be created.")
                .hasArg()
                .argName(ASSUMPTION_OUTPUT_FILE)
                .build();

        options.addOption(cnfFile);
        options.addOption(help);
        options.addOption(amountOfSolver);
        options.addOption(preprocess);
        options.addOption(cnfOutputFile);
        options.addOption(assumptionOutputFile);
    }

    @Override
    public void run(String... strings) throws Exception {
        if (strings == null || strings.length < 1) {
            formatter.printHelp("Main", options);
            return;
        }

        CommandLine line;
        try {
            line = parser.parse( options, strings );
        }  catch( ParseException exp ) {
            // oops, something went wrong
            log.error( "Parsing failed.  Reason: ", exp);
            formatter.printHelp("Main", options);
            return;
        }

        if (line.hasOption(HELP)) {
            formatter.printHelp("Main", options);
            return;
        }

        String cnfFName = line.getOptionValue(CNF_FILE);
        if (cnfFName == null) {
            log.error("No input cnf provided");
            return;
        }
        Cnf cnf = Cnf.fromStream(new FileInputStream(cnfFName));

        boolean preprocess = line.hasOption(PREPROCESS);
        if (preprocess) {
            cnf = minisatPreprocessService.preprocess(cnf);
            String outPath = line.getOptionValue(CNF_OUTPUT_FILE);
            File out =  StringUtils.isEmpty(outPath) ? File.createTempFile("Cnf-OUT", ".cnf")
                                                     : new File(outPath);
            log.info("Writing result of processed cnf to {} ..", out.getAbsolutePath());
            BufferedWriter writer = Files.newBufferedWriter(out.toPath());
            cnf.toDimacs().forEach(f ->
                    {
                        try {
                            writer.write(f + " \n");
                        } catch (IOException e) {
                            log.error("Could not write to file ..", e);
                        }
                    }
            );
            writer.close();
        }

        int nsolvers;
        try {
            nsolvers = Integer.parseInt(line.getOptionValue(NSOLVERS));
            if (nsolvers < 1) {
                log.error("nsolvers was not set correctly ..");
                return;
            }
        } catch (NumberFormatException e) {
            log.error("nsolvers was not set correctly ..");
            return;
        }

        SolverAssignments assignments = solverAssignmentService.simpleConfig(cnf, nsolvers);
        String outPath = line.getOptionValue(ASSUMPTION_OUTPUT_FILE);
        File out =  StringUtils.isEmpty(outPath) ? File.createTempFile("ASSUMPTIONS-OUT", ".cnf")
                                                 : new File(outPath);
        log.info("Writing result of generated assumptions to {} ..", out.getAbsolutePath());
        BufferedWriter writer = Files.newBufferedWriter(out.toPath());
        assignments.toDimacs().forEach(f ->
                {
                    try {
                        writer.write(f + " \n");
                    } catch (IOException e) {
                        log.error("Could not write to file ..", e);
                    }
                }
        );
        writer.close();

    }


}
