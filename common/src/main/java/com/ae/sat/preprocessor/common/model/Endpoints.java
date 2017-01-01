package com.ae.sat.preprocessor.common.model;

/**
 * Created by ae on 21-11-16.
 */
public class Endpoints {

    //
    public final static String MASTER_NAME = "MASTER";

    // Worker Endpoints
    public final static String READ_FILE_ENDPOINT = "/api/readfile";
    public final static String NEW_CLAUSES_ENDPOINT = "/api/clauses";
    public final static String STATS_ENDPOINT = "/api/stats.stream";
    public final static String LEARNTS_ENDPOINT = "/api/learnts.stream";
    public final static String ANSWER_ENDPOINT = "/api/answer.stream";
    public final static String READY_ENDPOINT = "/api/ready";
    public final static String RESET_ENDPOINT = "/api/reset";

    // Master Endpoints
    public final static String JOB_PROGRESS = "/api/jobprogress";
    public final static String JOB_PROGRESS_UPDATE = "/topic/jobprogress.stream";
}
