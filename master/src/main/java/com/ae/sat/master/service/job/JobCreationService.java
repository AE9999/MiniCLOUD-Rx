package com.ae.sat.master.service.job;

import com.ae.sat.master.service.connection.Connection;
import com.ae.sat.preprocessor.common.file.PersistenceAccess;
import com.ae.sat.preprocessor.common.model.formulas.Cnf;
import com.ae.sat.preprocessor.common.model.formulas.SolverAssignments;
import com.ae.sat.master.model.JobRequest;
import com.ae.sat.master.service.connection.DockerConnectionService;
import com.ae.sat.master.service.connection.PetConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ae on 26-10-16.
 */

@Component
public class JobCreationService {

    @Autowired
    private DockerConnectionService dockerService;

    @Autowired
    private PetConnectionService petConnectionService;

    @Autowired
    private PersistenceAccess persistenceAccess;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private int returnedSolver = 0;

    private Map<String, Job> id2jobs = Collections.synchronizedMap(new HashMap<>());

    public Job getJobById(String id) {
        return id2jobs.get(id);
    }

    public Job getDefaulJob() {
        if (id2jobs.isEmpty()) { return null; }
        return id2jobs.values().iterator().next();
    }

    public Job createJob(final JobRequest jobRequest) throws IOException {
        String prefix = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        List<SolverConnection> solverConnections;

        solverConnections = IntStream.range(0, jobRequest.getNsolvers())
                                     .mapToObj(f -> {
                                         boolean local = jobRequest.isUseDocker();
                                         String name = prefix.toUpperCase() + "-Rx-Worker-" + (++returnedSolver);
                                         Connection connection = local ? dockerService.getNewConnection(name)
                                                                       : petConnectionService.getNewConnection();
                                         return new SolverConnection(name, connection);
                                     })
                                     .collect(Collectors.toList());

        if (jobRequest.getCnfIS() == null && StringUtils.isEmpty(jobRequest.getCnfFName())) {
            throw new IllegalStateException("No CNF specified in request ..");
        }

        InputStream cnfIs = jobRequest.getCnfIS() != null ? jobRequest.getCnfIS()
                                                          : persistenceAccess.getInputStream(jobRequest.getCnfFName());

        if (jobRequest.getCnfIS() == null && StringUtils.isEmpty(jobRequest.getCnfFName())) {
            throw new IllegalStateException("No Assumption file specified in request ..");
        }

        boolean specified = jobRequest.getAssignmentIS() != null;
        InputStream assignmentsIS;
        assignmentsIS = specified ? jobRequest.getAssignmentIS()
                                  : persistenceAccess.getInputStream(jobRequest.getAssumptionFName());

        Cnf cnf = Cnf.fromStream(cnfIs);
        String cnfName = jobRequest.getCnfFName();
        SolverAssignments sa = SolverAssignments.fromStream(assignmentsIS);

        Job job = new Job(cnf, cnfName, sa, solverConnections);
        id2jobs.put(job.getId(), job);
        return job;
    }
}
