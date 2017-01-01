package com.ae.sat.preprocessor.common.model.formulas;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ae on 21-5-16.
 */
public class Cnf {

    private Clauses clauses;

    public Cnf() {}

    public Cnf(List<Clause> clauses) {
        this.clauses = new Clauses(clauses);
    }

    public Cnf(Clause... clauses) {
        this(new ArrayList<>(Arrays.asList(clauses)));
    }

    public Clauses getClauses() {
        return clauses;
    }


    public Map<Integer, Long> amountOfOccurences() {
        return this.clauses.getClauses().stream()
                                        .flatMap(c -> c.getLiterals().stream())
                                        .mapToInt(l -> l.getVar())
                                        .boxed()
                                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public int nVars() {
        // Yeah fuck the json stuff, anyway don't call this too often :-)
        return this.clauses.getClauses().stream()
                                        .map(f -> f.nVars())
                                        .sorted(Collections.reverseOrder())
                                        .findFirst()
                                        .get();
    }

    public static List<Literal> toList(int[] values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            literals.add(new Literal(values[i]));
        }
        return literals;
    }

    public static Cnf toFormula(int[][] values) {
        List<Clause> clauses = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            clauses.add(Clause.toClause(values[i]));
        }
        return new Cnf(clauses);
    }

    public Stream<String> toDimacs() {
        Stream<String> header = Stream.of(String.format("p cnf %s %s",
                                                        nVars(),
                                                        clauses.getClauses().size()));
        Stream<String> clauses = this.clauses.getClauses().stream()
                                                          .map(f -> f.toDimacs());
        return Stream.concat(header, clauses);
    }

    public static Cnf fromStream(InputStream is) throws IOException {

        Logger log = LoggerFactory.getLogger(Cnf.class);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        String name = null;
        List<Clause> clauses = new ArrayList<>();
        boolean started= false;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Content-Disposition:")) {
                //
                // Yeah I'm going to hell for this one:
                // Basically parsing Content-Disposition: form-data; name="file"; filename="{{fname}}"
                //
                name = line.split(" ")[3].substring("filename=\"".length());
                name = name.substring(0, name.length() - 1);
            }
            if (line.startsWith("p cnf ")) {
                started = true;
            }
            if (!started) { continue; }
            if (line.startsWith("c") || line.startsWith("p")) { continue; }
            String[] data = line.split("\\s+");
            if (data.length == 0) {
                throw new IllegalStateException("Could not parse Cnf"); }
            else if (data.length == 1) {
                clauses.add(new Clause(new ArrayList<>()));
            } else if (!"0".equals(data[data.length -1])) {
                throw new IllegalStateException("Could not parse Cnf");
            }
            List<Integer> lits = new ArrayList<>();
            for (int i = 0; i < data.length - 1; i++) {
                try {
                    lits.add(Integer.parseInt(data[i]));
                } catch (NumberFormatException e) {
                    // yeay fuck bad java stuff ..
                    log.error("Fix this stuff ..", e);
                }
            }
            clauses.add(Clause.toClause(lits));
        }
        Cnf rvalue =  new Cnf(clauses);
        return rvalue;
    }
}
