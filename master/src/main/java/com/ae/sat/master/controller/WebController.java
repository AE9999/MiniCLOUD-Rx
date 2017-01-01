package com.ae.sat.master.controller;

import com.ae.sat.master.model.JobRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by ae on 17-12-16.
 */

@RestController
public class WebController extends AbstractController {

    @RequestMapping(method = RequestMethod.GET, value = "/api/up")
    public HttpStatus ready() {
        return HttpStatus.OK;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/api/submit")
    public HttpStatus submitProblem(@RequestParam("nsolvers") int nsolvers,
                                    @RequestParam("cnfFile") MultipartFile cnfFile,
                                    @RequestParam(value = "cnfFName", required = false) final String cnfFName,
                                    @RequestParam("assumptionFile") MultipartFile assumptionFile,
                                    @RequestParam(value = "assumptionFName", required = false) String assumptionFName,
                                    @RequestParam(value = "useDocker", required = false) Boolean useDocker,
                                    @RequestParam(value = "sendCNF", required = false) Boolean sendCNF) throws IOException {

        JobRequest jobRequest;
        jobRequest = JobRequest.fromParameters(cnfFile != null ? cnfFName : cnfFile.getName(),
                                               nsolvers,
                                               cnfFName,
                                               cnfFile != null ? cnfFile.getInputStream() : null,
                                               assumptionFName,
                                               assumptionFile != null ? assumptionFile.getInputStream() : null,
                                               useDocker != null && useDocker.booleanValue(),
                                               sendCNF == null || sendCNF.booleanValue());

        handleJobRequest(jobRequest);

        return HttpStatus.OK;
    }
}
