package com.ae.sat.preprocessor.common.model;

/**
 * Created by ae on 24-5-16.
 */
public class Stats {
    private long toldUnits;
    private long includedUnits;
    private long exportedUnits;
    private long conflicts;
    private long runningTime;

    public long getToldUnits() {
        return toldUnits;
    }

    public void setToldUnits(long toldUnits) {
        this.toldUnits = toldUnits;
    }

    public long getIncludedUnits() {
        return includedUnits;
    }

    public void setIncludedUnits(long includedUnits) {
        this.includedUnits = includedUnits;
    }

    public long getExportedUnits() {
        return exportedUnits;
    }

    public void setExportedUnits(long exportedUnits) {
        this.exportedUnits = exportedUnits;
    }

    public long getConflicts() {
        return conflicts;
    }

    public void setConflicts(long conflicts) {
        this.conflicts = conflicts;
    }

    public long getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "toldUnits=" + toldUnits +
                ", includedUnits=" + includedUnits +
                ", exportedUnits=" + exportedUnits +
                ", conflicts=" + conflicts +
                ", runningTime=" + runningTime + " (s)" +
                '}';
    }

    public static Stats empty() {
        Stats stats = new Stats();
        stats.setIncludedUnits(0);
        stats.setToldUnits(0);
        stats.setConflicts(0);
        stats.setExportedUnits(0);
        stats.setRunningTime(0);
        return stats;
    }
}
