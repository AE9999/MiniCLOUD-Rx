package com.ae.sat.master.service.connection;

import com.ae.sat.master.service.sh.ShRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ae on 17-12-16.
 */
@Component
public class PetConnectionService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${workerDomain}")
    private String workerDomain;

    @Value("${workerName}")
    private String workerName;

    @Value("${workerPort}")
    private int workerPort;

    @Value("${workerWsPort}")
    private int workerWsPort;

    @Autowired
    private ShRunner shRunner;

    private Set<Integer> pool = new HashSet<>();

    @PostConstruct
    private void init() {
        int maxWorkers = 0;
        try {
            String command = "nslookup -type=srv " + workerDomain + ".default  " +
                             " | grep " + workerDomain +
                             " | wc -l; " +
                             " test ${PIPESTATUS[0]} -eq 0";
            List<String> results =  shRunner.executeCommandAsShArgument(command);
            maxWorkers = Integer.parseInt(results.get(0));
        } catch (NumberFormatException | IOException e) {
            log.warn("Could not initialize workers", e);
        }
        if (maxWorkers >= 1) {
            log.info("Found {} workers to be used ..", maxWorkers);
            pool.addAll(IntStream.range(0, maxWorkers).boxed().collect(Collectors.toSet()));
        }
    }

    private String getWorker(int id) {
        return workerName + "-" + id + "." + workerDomain;
    }

    public Connection getNewConnection() {

        final int nextId;
        synchronized (pool) {
            if (pool.isEmpty()) {
                throw new IllegalStateException("Worker Pool is empty");
            }
            Integer id = pool.iterator().next();
            pool.remove(id);
            nextId = id;
        }

        return new Connection() {
            @Override
            public int httpPort() {
                return workerPort;
            }

            @Override
            public int wsPort() {
                return workerWsPort;
            }

            @Override
            public String host() {
                return getWorker(nextId);
            }

            @Override
            public void close() throws IOException {
                synchronized (pool) {
                    pool.add(nextId);
                }
            }
        };
    }
}
