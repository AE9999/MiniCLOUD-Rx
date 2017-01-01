package com.ae.sat.preprocessor.common.servers.worker.service;

import com.ae.sat.preprocessor.common.model.formulas.Assumptions;
import com.ae.sat.preprocessor.common.model.formulas.Clause;
import com.ae.sat.preprocessor.common.model.formulas.Answer;
import com.ae.sat.preprocessor.common.model.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ae on 6-6-16.
 */
public abstract class SolverService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected long importedLearntClauses = 0;

    protected long toldLearntClauses = 0;

    protected long toldClauses = 0;

    protected long exportedClauses = 0;

    protected List<Clause> incomingClauses = new ArrayList<>();

    private ReplaySubject<Clause> learnts_ = ReplaySubject.create(10);

    private ReplaySubject<Stats> stats_ = ReplaySubject.create(1);

    public abstract void setConfig(byte[] rawConfigData);

    public abstract Answer doSolve(Assumptions assumptions);

    public abstract void reset();

    public Observable<Clause> learnts() {
        return learnts_;
    }

    public Observable<Stats> stats() {
        return stats_;
    }

    protected void publish(Clause learnt) {
        exportedClauses++;
        learnts_.onNext(learnt);
    }

    protected void publish(Stats stats) {
        stats_.onNext(stats);
    }

    public void addClause(Clause clause) {
        synchronized (incomingClauses) {
            toldClauses++;
            if (clause.isLearnt()) { toldLearntClauses++; }
            incomingClauses.add(clause);
        }
    }

    public Observable<Answer> solve(Assumptions assumptions) {
        log.info("Starting to solve under {} ..", assumptions);
        return Observable.just(doSolve(assumptions));
    }
}
