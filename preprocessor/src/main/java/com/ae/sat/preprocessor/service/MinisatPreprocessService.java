package com.ae.sat.preprocessor.service;

import com.ae.sat.preprocessor.common.model.formulas.Cnf;
import com.ae.sat.preprocessor.common.model.formulas.Clause;
import com.ae.sat.preprocessor.common.model.formulas.Literal;
import org.bytedeco.javacpp.minisat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ae on 11-6-16.
 */

@Component
public class MinisatPreprocessService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    minisat.SimpSolver simpSolver;

    private minisat.Lit lit2MinisatLit(Literal lit) {
        return minisat.mkLit(lit.getVar(), lit.isSigned());
    }

    private minisat.LitVecPointer clause2LitVecPointer(Clause clause) {
        minisat.LitVecPointer rvalue = new minisat.LitVecPointer();
        for (Literal lit : clause.getLiterals()) {
            rvalue.push(lit2MinisatLit(lit));
        }
        return rvalue;
    }

    private void assertFormula(Cnf cnf) {
        int nvars = cnf.nVars();
        while (simpSolver.nVars() <= nvars) {
            simpSolver.newVar();
        }
        for (Clause clause : cnf.getClauses().getClauses()) {
            minisat.LitVecPointer lvp = clause2LitVecPointer(clause);
            simpSolver.addClause(lvp);
        }
    }

    private Literal minisatLit2Literal(minisat.Lit minisatLiteral) {
        return new Literal(minisat.var(minisatLiteral), minisat.sign(minisatLiteral));
    }

    private Clause minisatClause2Clause(minisat.Clause minisatClause) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < minisatClause.size(); i++) {
            literals.add(minisatLit2Literal(minisatClause.get(i)));
        }
        return new Clause(literals);
    }

    public Cnf preprocess(Cnf cnf) {
        log.info("Applying MiniSat Style Preprocessing ..");
        simpSolver = new minisat.SimpSolver();
        assertFormula(cnf);
        simpSolver.eliminate();
        minisat.ClauseIterator it = simpSolver.clausesBegin();

        List<Clause> clauses = new ArrayList<>();
        while (it.notEquals(simpSolver.clausesEnd())) {
            minisat.Clause clause = it.multiply(); // Yeah shitty name;
            it.increment();
            clauses.add(minisatClause2Clause(clause));
        }
        log.info("Done with MiniSat Style Preprocessing ..");
        return new Cnf(clauses);
    }
}
