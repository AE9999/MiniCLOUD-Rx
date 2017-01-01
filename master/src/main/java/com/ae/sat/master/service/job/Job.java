package com.ae.sat.master.service.job;

/**
 * Created by ae on 28-10-16.
 */
import com.ae.sat.preprocessor.common.model.formulas.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by ae on 28-5-16.
 */
public class Job {

    private String id = UUID.randomUUID().toString().replace("-", "");

    private Logger log = LoggerFactory.getLogger(getClass());

    private ReplaySubject<Answer> finalAnswer = ReplaySubject.create(1);

    private ReplaySubject<Pair<String, Integer>> nextAssignmentNotification
            = ReplaySubject.create(1);

    private Observable<Clause> globalLearnts;

    private LocalDateTime startingTime = LocalDateTime.now();

    private LocalDateTime endTime = null;

    private Cnf cnf;

    private String cnfName;

    private List<Assumptions> assumptions = new ArrayList<>();

    private List<SolverConnection> solverConnections = new ArrayList<>();

    private int originalNrOfAssignments;

    private int solvedInstances;

    private boolean seenSat;

    public Job(Cnf cnf,
               String cnfName,
               SolverAssignments solverAssignments,
               List<SolverConnection> solverConnections) {
        this.cnf = cnf;
        this.cnfName = cnfName;
        this.solverConnections.addAll(solverConnections);
        this.assumptions.addAll(solverAssignments.getAssignments());
        this.originalNrOfAssignments = this.assumptions.size();
        this.solvedInstances = 0;
        this.seenSat = false;
    }

    public String getId() {
        return id;
    }

    public Cnf getCnf() {
        return cnf;
    }

    public String getCnfName() {
        return cnfName;
    }

    public int getOriginalNrOfAssignments() {
        return originalNrOfAssignments;
    }

    private Assumptions getAssignment() {
        synchronized (assumptions) {
            if (assumptions.isEmpty()) {
                return null;
            }
            return assumptions.remove(0);
        }
    }

    private Completable createCompletable(final SolverConnection solverConnection) {
        Completable completable = Completable.fromAction(() -> {
            solverConnection.start(cnf, cnfName);

            Assumptions currentAssumptions = getAssignment();
            while (currentAssumptions != null) {
                Answer answer = null;
                try {
                    answer = solverConnection.solve(currentAssumptions);
                    synchronized (assumptions) {
                        solvedInstances++;
                        if (answer == Answer.SAT) {
                            assumptions.clear();
                            seenSat = true;
                        }
                        nextAssignmentNotification.onNext(
                                new ImmutablePair<>(solverConnection.getName(),
                                                    solverConnection.getSolvedInstances())
                        );
                    }
                } catch (IOException e) {
                    log.error("Could not get answer", e);
                }
                currentAssumptions = getAssignment();
            }
        });
        return completable.subscribeOn(Schedulers.newThread());
    }

    public void start() {
       List<Observable<Clause>> learnts = solverConnections.stream()
                                                             .map(f -> f.getLearnts())
                                                             .collect(Collectors.toList());
        globalLearnts = Observable.merge(learnts);
        solverConnections.stream()
                         .forEach(solver -> solver.setGlobalLearnts(globalLearnts));

        Completable.merge(solverConnections.stream()
                                           .map(this::createCompletable)
                                           .collect(Collectors.toList()))
                   .await();
        endTime = LocalDateTime.now();
        finalAnswer.onNext(seenSat ? Answer.SAT : Answer.UNSAT);
    }

    public int getSolvedAssignments() {
        return solvedInstances;
    }

    public Observable<Answer> answer() {
        return finalAnswer;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<SolverConnection> getSolverConnections() {
        return solverConnections;
    }

    public ReplaySubject<Answer> getFinalAnswer() {
        return finalAnswer;
    }

    public Observable<Pair<String, Integer>> getNextAssignmentNotification() {
        return nextAssignmentNotification;
    }
}
