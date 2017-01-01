package com.ae.sat.master.controller;

import com.ae.sat.master.model.JobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.apache.commons.cli.*;

import javax.annotation.PostConstruct;

/**
 * Created by ae on 14-11-16.
 */
@Component
public class CommandLineController extends AbstractController implements CommandLineRunner {

    private static String NSOLVERS = "nsolvers";

    private static String HELP = "help";
    private static String CNF_FILE = "cnfFile";
    private static String ASSUMPTION_FILE = "assumptionFile";
    private static String USE_DOCKER = "useDocker";
    private static String SEND_CNF = "sendCNF";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Options options;

    private CommandLineParser parser;

    private HelpFormatter formatter;

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

        Option assumptionFile = Option.builder(ASSUMPTION_FILE)
                                      .desc("the location of the assumption file to be used.")
                                      .hasArg()
                                      .argName(ASSUMPTION_FILE)
                                      .build();

        Option help = Option.builder(HELP)
                            .desc("Print this message and quit.")
                            .build();

        Option amountOfSolver = Option.builder(NSOLVERS)
                                      .desc("Amount of solvers to be used.")
                                      .hasArg()
                                      .argName(NSOLVERS)
                                      .build();

        Option docker = Option.builder(USE_DOCKER)
                .desc("Optional flag, if set to true, the master will deploy its own workers using docker. " +
                      "If false it will look for available workers on the cluster. Default true.")
                .hasArg()
                .argName(USE_DOCKER)
                .build();

        Option sendCNF = Option.builder(SEND_CNF)
                .desc("Optional flag, if set to true, the master will send copy of Cnf to workers.")
                .hasArg()
                .argName(SEND_CNF)
                .build();

        options.addOption(cnfFile);
        options.addOption(assumptionFile);
        options.addOption(help);
        options.addOption(amountOfSolver);
        options.addOption(docker);
        options.addOption(sendCNF);
    }

    @Override
    public void run(String... strings) throws Exception {

        if (strings == null || strings.length < 1) {
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

        String cnfFName = line.getOptionValue(CNF_FILE);
        String solverAssignmentsFName = line.getOptionValue(ASSUMPTION_FILE);
        boolean useDocker = (!line.hasOption(USE_DOCKER))
                             || Boolean.parseBoolean(line.getOptionValue(USE_DOCKER));
        boolean sendCNF = (!line.hasOption(SEND_CNF))
                            || Boolean.parseBoolean(line.getOptionValue(SEND_CNF));

        JobRequest jobRequest;
        jobRequest = JobRequest.fromParameters(cnfFName,
                                               nsolvers,
                                               cnfFName,
                                               null,
                                               solverAssignmentsFName,
                                               null,
                                               useDocker,
                                               sendCNF);

        handleJobRequest(jobRequest);
    }
}
