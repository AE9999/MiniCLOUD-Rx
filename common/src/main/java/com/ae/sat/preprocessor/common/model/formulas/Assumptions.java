package com.ae.sat.preprocessor.common.model.formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ae on 2-12-16.
 */
public class Assumptions {
    private List<Literal> literals;

    public Assumptions() {
        literals = new ArrayList<>();
    }

    public Assumptions(List<Literal> literals) {
        this();
        this.literals.addAll(literals);
    }

    public Assumptions(Literal... literals) {
        this(Arrays.asList(literals));
    }

    public List<Literal> getLiterals() {
        return literals;
    }

    public void setLiterals(List<Literal> literals) {
        this.literals = literals;
    }

    @Override
    public String toString() {
        return "Assumptions{" +
                "literals=" + literals +
                '}';
    }

    public static Assumptions toLiterals(int[] values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            literals.add(new Literal(values[i]));
        }
        return new Assumptions(literals);
    }

    public static Assumptions toLiterals(List<Integer> values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            literals.add(new Literal(values.get(i)));
        }
        return new Assumptions(literals);
    }

    public String toDimacs() {
        StringBuilder sb = new StringBuilder();
        sb.append("a");
        for (Literal l : literals) {
            sb.append(" ");
            sb.append(l.toDimacs());
        }
        sb.append(" 0");
        return sb.toString();
    }
}
