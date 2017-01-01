package com.ae.sat.master.controller;

import com.ae.sat.master.model.JobRequest;
import com.ae.sat.master.service.WebInterfaceStarterService;
import com.ae.sat.master.service.job.Job;
import com.ae.sat.master.service.job.JobBroadcastUpdateService;
import com.ae.sat.master.service.job.JobCreationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Created by ae on 30-12-16.
 */

public class AbstractController {

    @Autowired
    private JobCreationService jobCreationService;

    @Autowired
    private JobBroadcastUpdateService jobBroadcastUpdateService;

    @Autowired
    private WebInterfaceStarterService webInterfaceStarterService;

    public void handleJobRequest(JobRequest jobRequest) throws IOException {
        Job job = jobCreationService.createJob(jobRequest);

        new Thread(() -> job.start()).start();
        jobBroadcastUpdateService.registerJob(job);
        webInterfaceStarterService.startWebInterface();
    }
}
