package com.ae.sat.preprocessor.common.model.formulas;

/**
 * Created by ae on 21-5-16.
 */
public class Literal {
    private int var;
    private boolean signed;

    public Literal() {

    }

    public Literal(int var, boolean signed) {
        if (var <= 0) {
            String m = "This constructor expects a positive integer argument";
            throw new IllegalArgumentException(m);
        }
        this.var = var;
        this.signed = signed;
    }

    public Literal(int var) {
        if (var == 0) {
            String m = "This constructor expects a non zero integer argument";
            throw new IllegalArgumentException(m);
        }
        this.var = var < 0 ? -1 * var : var;
        this.signed = var < 0;
    }

    public int getVar() {
        return var;
    }

    public void setVar(int var) {
        this.var = var;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    @Override
    public String toString() {
        return "Literal{" +
                "var=" + var +
                ", signed=" + signed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Literal literal = (Literal) o;

        if (var != literal.var) return false;
        return signed == literal.signed;

    }

    @Override
    public int hashCode() {
        int result = var;
        result = 31 * result + (signed ? 1 : 0);
        return result;
    }

    public String toDimacs() {
        return signed ? "-" + String.valueOf(var) : String.valueOf(var);
    }
}