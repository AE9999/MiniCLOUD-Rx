package com.ae.sat.preprocessor.common.model;

import com.ae.sat.preprocessor.common.model.formulas.Literal;

import java.util.List;

/**
 * Created by ae on 26-11-16.
 */
public class SolveRequest {

    List<Literal> assumptions;

    public List<Literal> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(List<Literal> assumptions) {
        this.assumptions = assumptions;
    }
}
