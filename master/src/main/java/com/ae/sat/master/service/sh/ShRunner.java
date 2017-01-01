package com.ae.sat.master.service.sh;

/**
 * Created by ae on 3-7-16.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by ae on 6-2-16.
 */

@Component
public class ShRunner {

    private final Logger log = LoggerFactory.getLogger(ShRunner.class);

    @Autowired
    public AsyncWorker selfReference; // Needed for the async stuff

    @Component
    public static class AsyncWorker
    {
        private final Logger log = LoggerFactory.getLogger(ShRunner.class);

        @Async
        public Future<List<String>> getOutput (InputStream is, String prefix) {
            List<String> readOutput = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line_out;
            try {
                while ((line_out = bufferedReader.readLine()) != null) {
                    log.debug(String.format("[%s] Read: %s ", prefix, line_out));
                    readOutput.add(line_out);
                }
            } catch (IOException e) {
                log.error("Error while reading process", e);
            }
            return new AsyncResult<>(readOutput);
        }
    }

    public List<String> executeCommandAsShArgument(String command) throws IOException {
        log.debug(String.format("Running %s ..", command));
        List<String> commands = Arrays.asList(new String[]{"sh", "-c", command});
        ProcessBuilder pb = new ProcessBuilder(commands);
        Process p = pb.start();
        String myId = UUID.randomUUID().toString();
        String stdErrPrefix = String.format("(%s) ERR", myId);
        String stdOutPrefix = String.format("(%s) OUT", myId);

        selfReference.getOutput(p.getErrorStream(), stdErrPrefix);
        try {
            List<String> rvalue = selfReference.getOutput(p.getInputStream(), stdOutPrefix).get();
            p.waitFor(); // Not nice but should work ..
            int exitValue = p.exitValue();
            log.debug(String.format("Process finished with exit code %d ..", exitValue));
            if(exitValue != 0) {
                String m = String.format("Process finished with non zero exit code %d ..",
                                         exitValue);
                throw new IOException(m);
            }
            return rvalue;
        } catch (InterruptedException | ExecutionException e) {
            log.error(String.format("Error while running %s ..", command), e);
            throw new IOException(e);
        }
    }
}
