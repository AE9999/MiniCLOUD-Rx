package com.ae.sat.master.model;

/**
 * Created by ae on 23-11-16.
 */
public class WorkerProgress {

    private String workerName;

    private String answerUpdate;

    private String statUpdate;

    private String solvedAssignmentsUpdate;

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getAnswerUpdate() {
        return answerUpdate;
    }

    public void setAnswerUpdate(String answerUpdate) {
        this.answerUpdate = answerUpdate;
    }

    public String getStatUpdate() {
        return statUpdate;
    }

    public void setStatUpdate(String statUpdate) {
        this.statUpdate = statUpdate;
    }

    public String getSolvedAssignmentsUpdate() {
        return solvedAssignmentsUpdate;
    }

    public void setSolvedAssignmentsUpdate(String solvedAssignmentsUpdate) {
        this.solvedAssignmentsUpdate = solvedAssignmentsUpdate;
    }
}
