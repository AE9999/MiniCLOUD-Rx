package com.ae.sat.master.model;

import com.ae.sat.preprocessor.common.model.formulas.Answer;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ae on 23-11-16.
 */
public class JobProgress {

    private String cnfName;

    public int vars;

    public int clauses;

    public Answer answer;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime endTime;

    public List<WorkerProgress> workerProgresses;

    private int originalAssignments;

    private int solvedAssignments;

    public String getCnfName() {
        return cnfName;
    }

    public void setCnfName(String cnfName) {
        this.cnfName = cnfName;
    }

    public int getVars() {
        return vars;
    }

    public void setVars(int vars) {
        this.vars = vars;
    }

    public int getClauses() {
        return clauses;
    }

    public void setClauses(int clauses) {
        this.clauses = clauses;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<WorkerProgress> getWorkerProgresses() {
        return workerProgresses;
    }

    public void setWorkerProgresses(List<WorkerProgress> workerProgresses) {
        this.workerProgresses = workerProgresses;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public void setOriginalAssignments(int originalAssignments) {
        this.originalAssignments = originalAssignments;
    }

    public int getOriginalAssignments() {
        return originalAssignments;
    }

    public void setSolvedAssignments(int solvedAssignments) {
        this.solvedAssignments = solvedAssignments;
    }

    public int getSolvedAssignments() {
        return solvedAssignments;
    }
}

