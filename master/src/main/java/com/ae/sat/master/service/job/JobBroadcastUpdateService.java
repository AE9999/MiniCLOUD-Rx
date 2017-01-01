package com.ae.sat.master.service.job;

import com.ae.sat.master.model.WorkerProgress;
import com.ae.sat.preprocessor.common.model.Endpoints;
import com.ae.sat.preprocessor.common.model.Stats;
import com.ae.sat.preprocessor.common.model.formulas.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Created by ae on 23-11-16.
 */

@Component
public class JobBroadcastUpdateService {

    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;


    public void registerWorker(final SolverConnection worker) {
        final LocalDateTime now = LocalDateTime.now();

        worker.getStats().filter(f -> !(f.equals(Stats.empty()))).subscribe(stats -> {
            //
            // Yes another hack
            //
            stats.setRunningTime(Duration.between(now, LocalDateTime.now()).getSeconds());
            broadcastUpdate(worker.getName(), stats, null, -1);
        });
    }

    public void registerJob(Job job) {
        job.getSolverConnections().stream().forEach(this::registerWorker);
        job.getNextAssignmentNotification().subscribe(f -> {
            broadcastUpdate(f.getLeft(), null, null, f.getRight());
            broadcastUpdate(Endpoints.MASTER_NAME, null, null, job.getSolvedAssignments());
        });
        job.answer().filter(f -> f != Answer.UNKOWN).subscribe(answer -> {
            broadcastUpdate(Endpoints.MASTER_NAME, null, answer, -1);
        });
    }

    private void broadcastUpdate(String name, Stats stats, Answer answer, int solvedAssignments) {
        WorkerProgress workerProgress = new WorkerProgress();
        workerProgress.setWorkerName(name);
        workerProgress.setAnswerUpdate(answer != null ? answer.toString() : "");
        workerProgress.setStatUpdate(stats != null ? stats.toString() : "");
        workerProgress.setSolvedAssignmentsUpdate(solvedAssignments >= 0 ? String.valueOf(solvedAssignments)
                                                                         : "");
        brokerMessagingTemplate.convertAndSend(Endpoints.JOB_PROGRESS_UPDATE,
                                               workerProgress);
    }

}
