package com.ae.sat.preprocessor.common.model.formulas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ae on 30-12-16.
 */
public class SolverAssignments {

    public static String SEPERATOR = "\n";

    private List<Assumptions> assignments;

    public SolverAssignments() {
        assignments = new ArrayList<>();
    }

    public SolverAssignments(List<Assumptions> assignments) {
        this();
        this.assignments.addAll(assignments);
    }

    public List<Assumptions> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assumptions> assignments) {
        this.assignments = assignments;
    }

    public SolverAssignments(Assumptions... assignments) {
        this(Arrays.asList(assignments));
    }

    public static SolverAssignments fromStream(InputStream is) throws IOException {

        Logger log = LoggerFactory.getLogger(SolverAssignments.class);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        String name = null;
        List<Assumptions> assignments = new ArrayList<>();
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
            if (!line.startsWith("a ")) { continue; }
            String[] data = line.split("\\s+");
            if (data.length == 0) {
                throw new IllegalStateException("Could not parse assignments"); }
            else if (data.length == 1) {
                assignments.add(new Assumptions(new ArrayList<>()));
            } else if (!"0".equals(data[data.length -1])) {
                throw new IllegalStateException("Could not parse assignments");
            }
            List<Integer> lits = new ArrayList<>();
            for (int i = 1; i < data.length - 1; i++) {
                try {
                    lits.add(Integer.parseInt(data[i]));
                } catch (NumberFormatException e) {
                    // yeay fuck bad java stuff ..
                    log.error("Fix this stuff ..", e);
                }
            }
            assignments.add(Assumptions.toLiterals(lits));
        }
        SolverAssignments rvalue =  new SolverAssignments(assignments);
        return rvalue;
    }

    public Stream<String> toDimacs() {
        return assignments.stream().map(f -> f.toDimacs());
    }
}
