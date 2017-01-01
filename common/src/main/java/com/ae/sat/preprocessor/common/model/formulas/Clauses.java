package com.ae.sat.preprocessor.common.model.formulas;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ae on 2-12-16.
 */
public class Clauses {

    public static String SEPERATOR = "\n";

    private List<Clause> clauses;

    public Clauses() {
        clauses = new ArrayList<>();
    }

    public Clauses(List<Clause> clauses) {
        this();
        this.clauses.addAll(clauses);
    }

    public Clauses(Clause... clauses) {
        this(Arrays.asList(clauses));
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    public void setClauses(List<Clause> clauses) {
        this.clauses = clauses;
    }

    public static String getBodyForStreaming(Clauses cls) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();//  body;
        for (Clause clause : cls.clauses) {
            sb.append(objectMapper.writeValueAsString(clause) + SEPERATOR);
        }
        return sb.toString();
    }
}
