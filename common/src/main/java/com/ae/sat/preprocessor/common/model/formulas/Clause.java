package com.ae.sat.preprocessor.common.model.formulas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ae on 21-5-16.
 */
public class Clause implements Serializable {

    private boolean learnt;

    private List<Literal> literals;

    public Clause() {
    }

    public Clause(Literal... literals) {
        this(false, Arrays.asList(literals));
    }

    public Clause(List<Literal> literals) {
        this(false, literals);
    }

    public Clause(boolean learnt, Literal... literals) {
        this(learnt, Arrays.asList(literals));
    }

    public Clause(boolean learnt, List<Literal> literals) {
        this.literals = literals;
        this.learnt = learnt;
    }

    public List<Literal> getLiterals() {
        return literals;
    }

    public boolean isLearnt() {
        return learnt;
    }

    public int lenght() {
        return literals.size();
    }

    public int nVars() {
        return literals.stream()
                       .sorted((o1, o2) -> -1 * Integer.compare(o1.getVar(), o2.getVar()))
                       .mapToInt(f -> f.getVar())
                       .findFirst().getAsInt();
    }

    public boolean contains(Literal lit) {
        return literals.contains(lit);
    }

    public boolean subsumes(Clause other) {
        if (other == null || other.lenght() <= lenght()) {
            return false;
        }
        return literals.stream().anyMatch(l -> !other.contains(l));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Clause clause = (Clause) o;

        if (learnt != clause.learnt) return false;
        return literals != null ? literals.equals(clause.literals) : clause.literals == null;

    }

    @Override
    public int hashCode() {
        int result = (learnt ? 1 : 0);
        result = 31 * result + (literals != null ? literals.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Clause{" +
                "learnt=" + learnt +
                ", literals=" + literals +
                '}';
    }

    public static Clause toClause(int[] values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            literals.add(new Literal(values[i]));
        }
        return new Clause(literals);
    }

    public static Clause toClause(List<Integer> values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            literals.add(new Literal(values.get(i)));
        }
        return new Clause(literals);
    }

    public String toDimacs() {
        StringBuilder sb = new StringBuilder();
        for (Literal l : literals) {
            sb.append(sb.length() == 0 ? "" : " ");
            sb.append(l.toDimacs());
        }
        sb.append(" 0");
        return sb.toString();
    }
}

