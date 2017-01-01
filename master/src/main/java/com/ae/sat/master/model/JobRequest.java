package com.ae.sat.master.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ae on 14-11-16.
 */
public class JobRequest {

    private int nsolvers;

    private String cnfFName;

    private String assumptionFName;

    private boolean useDocker;

    private String name;

    private boolean sendCNF;

    private InputStream cnfIS;

    private InputStream assignmentIS;

    public int getNsolvers() {
        return nsolvers;
    }

    public void setNsolvers(int nsolvers) {
        this.nsolvers = nsolvers;
    }

    public String getCnfFName() {
        return cnfFName;
    }

    public void setCnfFName(String cnfFName) {
        this.cnfFName = cnfFName;
    }

    public String getAssumptionFName() {
        return assumptionFName;
    }

    public void setAssumptionFName(String assumptionFName) {
        this.assumptionFName = assumptionFName;
    }

    public boolean isUseDocker() {
        return useDocker;
    }

    public void setUseDocker(boolean useDocker) {
        this.useDocker = useDocker;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setSendCNF(boolean sendCNF) {
        this.sendCNF = sendCNF;
    }

    public boolean isSendCNF() {
        return sendCNF;
    }

    public InputStream getCnfIS() {
        return cnfIS;
    }

    public void setCnfIS(InputStream cnfIS) {
        this.cnfIS = cnfIS;
    }

    public InputStream getAssignmentIS() {
        return assignmentIS;
    }

    public void setAssignmentIS(InputStream assignmentIS) {
        this.assignmentIS = assignmentIS;
    }

    public static JobRequest fromParameters(String name,
                                            int nsolvers,
                                            String cnfFName,
                                            InputStream cnfIS,
                                            String assignmentFName,
                                            InputStream assignmentIS,
                                            boolean useDocker,
                                            boolean sendCNF) throws IOException {
        JobRequest jobRequest = new JobRequest();
        jobRequest.setName(name);
        jobRequest.setNsolvers(nsolvers);
        jobRequest.setCnfFName(cnfFName);
        jobRequest.setAssumptionFName(assignmentFName);
        jobRequest.setUseDocker(useDocker);
        jobRequest.setSendCNF(sendCNF);
        jobRequest.setCnfIS(cnfIS);
        jobRequest.setAssignmentIS(assignmentIS);
        return jobRequest;
    }
}
