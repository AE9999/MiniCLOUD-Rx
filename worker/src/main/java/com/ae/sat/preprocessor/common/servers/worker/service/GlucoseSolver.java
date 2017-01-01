package com.ae.sat.preprocessor.common.servers.worker.service;

import com.ae.sat.preprocessor.common.model.formulas.Answer;
import com.ae.sat.preprocessor.common.model.formulas.Clause;
import com.ae.sat.preprocessor.common.model.formulas.Literal;
import com.ae.sat.preprocessor.common.model.formulas.Assumptions;
import com.ae.sat.preprocessor.common.model.Stats;
import org.bytedeco.javacpp.glucose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by ae on 22-5-16.
 */

@Component
public class GlucoseSolver extends SolverService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private glucose.Solver solver = new glucose.Solver();

    private glucose.LitVecPointer assumptions2LitVecPointer(List<Literal> assumptions) {
        glucose.LitVecPointer rvalue = new glucose.LitVecPointer();
        for (Literal lit : assumptions) {
            while (solver.nVars() <= lit.getVar()) {
                solver.newVar();
            }
            rvalue.push(lit2GlucoseLit(lit));
        }
        return rvalue;
    }

    private glucose.LitVecPointer clause2LitVecPointer(Clause clause) {
        glucose.LitVecPointer rvalue = new glucose.LitVecPointer();
        for (Literal lit : clause.getLiterals()) {
            rvalue.push(lit2GlucoseLit(lit));
        }
        return rvalue;
    }

    private glucose.Lit lit2GlucoseLit(Literal lit) {
        return glucose.mkLit(lit.getVar(), lit.isSigned());
    }

    private Literal minisatLit2Lit(glucose.Lit lit) {
        return new Literal(glucose.var(lit), glucose.sign(lit));
    }

    @Override
    public void setConfig(byte[] rawConfigData) {

    }

    private void reserveVarsForClause(Clause clause) {
        int nvars = clause.nVars();
        while (solver.nVars() <= nvars) {
            solver.newVar();
        }
    }

    private Answer doAddLearntUnitClause(Clause clause) {
        glucose.Lit lit = lit2GlucoseLit(clause.getLiterals().get(0));
        if (solver.value(lit) == glucose.l_Undef) {
            log.info(String.format("Added %s as unit clause ..", lit));
            importedLearntClauses++;
            solver.addClause(lit);
        } else if (solver.value(lit) == glucose.l_False) {
            importedLearntClauses++;
            log.info(String.format("Found contradiction for %s ..", lit));
            return Answer.UNSAT;
        } else {
            log.info(String.format("%s was already satisfied ..", lit));
        }
        return Answer.UNKOWN;
    }

    private void doAddClause(Clause clause) {
        glucose.LitVecPointer lvp = clause2LitVecPointer(clause);
        solver.addClause(lvp);
    }

    private Answer assertOutStandingFormula() {
        synchronized (incomingClauses) {
            log.info("Importing unit clauses ..");

            for (Clause clause : incomingClauses) {
                if (clause.lenght() == 0) { return Answer.UNSAT; }

                reserveVarsForClause(clause);
                if (clause.isLearnt()
                    && clause.lenght() == 1
                    && doAddLearntUnitClause(clause) == Answer.UNSAT) {
                    return Answer.UNSAT;
                }
                doAddClause(clause);


            }
            log.info("Done ..");
            incomingClauses.clear();
        }
        return Answer.UNKOWN;
    }


    @Override
    public void reset() {
        solver.deallocate();
        solver = new glucose.Solver();
    }

    @Override
    public Answer doSolve(Assumptions assumptions) {
        glucose.LitVecPointer assumptions_;
        assumptions_ = assumptions2LitVecPointer(assumptions.getLiterals());

        int assignmentPointer = solver.nAssigns();
        while (true) {
            if (assertOutStandingFormula() == Answer.UNSAT) {
                return Answer.UNSAT;
            }

            log.info("Going through preliminary trail stuff after importing unit clauses ..");
            for (; assignmentPointer < solver.nAssigns(); assignmentPointer++) {}
            log.info("Done ..");

            log.info("Starting solving ..");
            solver.setConfBudget(5000);
            glucose.lbool res = solver.solveLimited(assumptions_);

            log.info(String.format("The result was %s ..", res));
            if (!res.equals(glucose.l_Undef())) {
                return res.equals(glucose.l_True()) ? Answer.SAT : Answer.UNSAT;
            }

            log.info("Publishing learnt assignments ..");
            // These are new assignements we have learnt
            for (; assignmentPointer < solver.nAssigns(); assignmentPointer++) {
                glucose.Lit s = solver.trail().get(assignmentPointer);
                Literal learnt = minisatLit2Lit(s);
                publish(new Clause(true, learnt));
            }
            log.info("Done ..");

            Stats stats  = new Stats();
            stats.setConflicts(solver.conflicts());
            stats.setToldUnits(toldLearntClauses);
            stats.setIncludedUnits(importedLearntClauses);
            stats.setExportedUnits(exportedClauses);
            publish(stats);
        }
    }
}
