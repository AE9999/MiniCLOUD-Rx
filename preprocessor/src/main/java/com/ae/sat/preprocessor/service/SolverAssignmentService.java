package com.ae.sat.preprocessor.service;

/**
 * Created by ae on 28-10-16.
 */
import com.ae.sat.preprocessor.common.model.formulas.Assumptions;
import com.ae.sat.preprocessor.common.model.formulas.Cnf;
import com.ae.sat.preprocessor.common.model.formulas.Literal;
import com.ae.sat.preprocessor.common.model.formulas.SolverAssignments;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by ae on 22-5-16.
 */

@Component
public class SolverAssignmentService {

    private boolean[] bitSetToArray(BitSet bs, int width) {
        boolean[] result = new boolean[width]; // all false
        bs.stream().forEach(i -> result[i] = true);
        return result;
    }

    List<boolean[]> bool(int n) {
        return IntStream.range(0, (int)Math.pow(2, n))
                .mapToObj(i -> bitSetToArray(BitSet.valueOf(new long[] { i }), n))
                .collect(toList());
    }

    public SolverAssignments simpleConfig(Cnf cnf, int amountOfSolvers) {
        if (amountOfSolvers <= 0) {
            throw new IllegalArgumentException("Need at least one solver.");
        }
        if (amountOfSolvers == 1) {
            return new SolverAssignments(new Assumptions());
        }
        if ((amountOfSolvers & (amountOfSolvers - 1)) != 0) {
            String m = "Expected amount of Solvers to be a power of 2";
            throw new IllegalArgumentException(m);
        }

        int amountOfIntsToFix = new Double(Math.log(amountOfSolvers) / Math.log(2)).intValue();
        Stream<Map.Entry<Integer, Long>> stream;
        stream = cnf.amountOfOccurences().entrySet().stream();
        List<Integer> vars = stream.sorted(Comparator.comparing(Map.Entry::getValue))
                                   .map(Map.Entry::getKey)
                                   .collect(toList());
        List<Assumptions> workerInputs = new ArrayList<>();
        for (boolean[] config : bool(amountOfIntsToFix)) {
            List<Literal> assumptions = new ArrayList<>();
            for (int i = 0; i < config.length; i++) {
                assumptions.add(new Literal(vars.get(i), config[i]));
            }
            workerInputs.add(new Assumptions(assumptions));
        }
        return new SolverAssignments(workerInputs);
    }
}
