package com.ae.sat.master.controller;

import com.ae.sat.master.model.JobProgress;
import com.ae.sat.master.model.WorkerProgress;
import com.ae.sat.master.service.job.Job;
import com.ae.sat.master.service.job.JobCreationService;
import com.ae.sat.master.service.job.SolverConnection;
import com.ae.sat.preprocessor.common.model.Endpoints;
import com.ae.sat.preprocessor.common.model.formulas.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by ae on 22-11-16.
 */

@RestController
public class JobProgressController {

    @Autowired
    private JobCreationService jobCreationService;

    private WorkerProgress solverConnectionToWorkerProgress(SolverConnection solverConnection) {
        WorkerProgress workerProgress = new WorkerProgress();
        workerProgress.setWorkerName(solverConnection.getName());
        workerProgress.setAnswerUpdate(String.valueOf(solverConnection.getSolvedInstances()));
        workerProgress.setSolvedAssignmentsUpdate("0");
        workerProgress.setStatUpdate(solverConnection.getStats().toBlocking().first().toString());
        return workerProgress;
    }

    private JobProgress JobToJobProgress(Job job) {

        List<WorkerProgress> workerProgres;
        workerProgres = job.getSolverConnections().stream()
                                                  .map(this::solverConnectionToWorkerProgress)
                                                  .collect(Collectors.toList());
        JobProgress jobProgress = new JobProgress();
        try {
            Answer a = job.getFinalAnswer().first().toBlocking().toFuture().get(1, TimeUnit.SECONDS);
            jobProgress.setAnswer(a);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // HACK expected error
            jobProgress.setAnswer(Answer.UNKOWN);
        }

        // TODO: FIX
        jobProgress.setClauses(job.getCnf().getClauses().getClauses().size());
        jobProgress.setVars(job.getCnf().nVars());
        jobProgress.setCnfName(job.getCnfName());
        jobProgress.setOriginalAssignments(job.getOriginalNrOfAssignments());
        jobProgress.setSolvedAssignments(job.getSolvedAssignments());
        jobProgress.setStartTime(job.getStartingTime());
        jobProgress.setEndTime(job.getEndTime());
        jobProgress.setWorkerProgresses(workerProgres);

        return jobProgress;
    }

    @RequestMapping(Endpoints.JOB_PROGRESS)
    public JobProgress getProgress() {
        return JobToJobProgress(jobCreationService.getDefaulJob());
    }

}
