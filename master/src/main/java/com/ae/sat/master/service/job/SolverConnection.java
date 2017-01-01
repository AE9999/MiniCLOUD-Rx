package com.ae.sat.master.service.job;

/**
 * Created by ae on 28-10-16.
 */
import com.ae.sat.master.service.connection.Connection;
import com.ae.sat.preprocessor.common.model.formulas.*;
import com.ae.sat.preprocessor.common.model.Endpoints;
import com.ae.sat.preprocessor.common.model.Stats;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.channel.StringTransformer;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.sse.ServerSentEvent;
import io.reactivex.netty.protocol.http.websocket.WebSocketClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 22-5-16.
 */
public class SolverConnection {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String name;

    private Connection connection;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ReplaySubject<Clause> learnts_ = ReplaySubject.create(10);

    private ReplaySubject<Stats> stats_ = ReplaySubject.createWithSize(1);

    private ReplaySubject<String> rawOutGoingClauses = ReplaySubject.create(10);

    private Pair<HttpClient<ByteBuf, ServerSentEvent>,
                 ObservableConnection<TextWebSocketFrame, TextWebSocketFrame>> client;

    private Set<Clause> seenLearntClauses = Collections.synchronizedSet(new HashSet<>());

    private int solvedInstances = 0;

    public SolverConnection(String name, Connection connection) {
        this.name = name;
        this.connection = connection;
        this.stats_.onNext(Stats.empty());
    }

    public String getName() {
        return name;
    }

    public int getSolvedInstances() { return solvedInstances; }

    public Observable<Stats> getStats() {
        return stats_;
    }

    public Observable<Clause> getLearnts() {
        return learnts_;
    }

    //
    // Returns the port the container started on.
    // client
    private Pair<HttpClient<ByteBuf, ServerSentEvent>,
                            ObservableConnection<TextWebSocketFrame,
                                                 TextWebSocketFrame>> connect(Connection connection)
            throws IOException,
                   InterruptedException,
                   ExecutionException,
                   TimeoutException {
        log.info("{} Starting solving job ..", name);

        PipelineConfigurator<HttpClientResponse<ServerSentEvent>,
                             HttpClientRequest<ByteBuf>> configurator;
        configurator = PipelineConfigurators.clientSseConfigurator();

        //
        // HACK: If one knows of a better way to test if the server is up
        // let me know :-)
        //
        HttpClient<ByteBuf, ServerSentEvent> client = null;
        Throwable error;
        do {
            try {
                error = null;
                client = RxNetty.createHttpClient(connection.host(), connection.httpPort(), configurator);
                Observable<HttpClientResponse<ServerSentEvent>> response;
                response = client.submit(HttpClientRequest.createPost(Endpoints.READY_ENDPOINT));
                response.toBlocking().toFuture().get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                error = e;
            }
        } while (error != null);
        log.debug("HTTP Client was obtained ..");

        WebSocketClient<TextWebSocketFrame, TextWebSocketFrame> rxClient;
        rxClient = RxNetty.<TextWebSocketFrame,
                            TextWebSocketFrame>newWebSocketClientBuilder(connection.host(),
                                                                         connection.wsPort())
                        .withWebSocketURI("/websocket")
                        .withWebSocketVersion(WebSocketVersion.V13)
                        .build();

        log.debug("Establishing WS connection ..");
        ObservableConnection<TextWebSocketFrame, TextWebSocketFrame> wsconnection;
        wsconnection = rxClient.connect().toBlocking().first();
        log.debug("Got the connection trying sending data ..");
        wsconnection.writeAndFlush(new TextWebSocketFrame("PING"));
        log.debug("Sending complete waiting for answer ..");
        String response = wsconnection.getInput()
                                    .first()
                                    .map(r -> r.text())
                                    .toBlocking()
                                    .toFuture()
                                    .get(10, TimeUnit.SECONDS);
        log.debug("Got my response {} ..", response);
        log.debug("WS Client was obtained ..");

        return new ImmutablePair<>(client, wsconnection);
    }

    private void handleIncomingClause(String raw) {
        try {
            Clause clause = objectMapper.readValue(raw, Clause.class);
            log.debug("{} Recieved {} as learnt lit ..", name, clause);
            seenLearntClauses.add(clause);
            learnts_.onNext(clause);
        } catch (IOException e) {
            log.error("Could not parse literal ", e);
        }
    }

    private void handleIncomingStats(String raw) {
        try {
            Stats stats = objectMapper.readValue(raw, Stats.class);
            log.debug("Recieved {} as stats ..", stats);
            stats_.onNext(stats);
        } catch (IOException e) {
            log.error("Could not parse stats ", e);
        }
    }

    private void initIncomingFlows(HttpClient<ByteBuf, ServerSentEvent> client) {

        //
        // Bas: Important subscribe to request or they won't do anything :-)
        //
        Observable<HttpClientResponse<ServerSentEvent>> response;

        log.debug("{} is subscribing to incoming clauses ..", name);
        response = client.submit(HttpClientRequest.createGet(Endpoints.LEARNTS_ENDPOINT));
        response.doOnError(f -> log.warn("A warning ..")) // HACK, fix missing stuff & gracious shutting down
                .flatMap(r -> r.getContent())
                .subscribe(f -> handleIncomingClause(f.contentAsString()));

        log.debug("{} is subscribing to stats ..", name);
        response = client.submit(HttpClientRequest.createGet(Endpoints.STATS_ENDPOINT));
        response.doOnError(f -> log.warn("A warning ..")) // HACK, fix missing stuff & gracious shutting down
                .flatMap(r -> r.getContent())
                .subscribe(f -> handleIncomingStats(f.contentAsString()));
        response.doOnError(f -> log.error("Could not subscribe to", f));
    }

    public Answer solve(Assumptions assumptions) throws IOException {
        log.debug("{} is subscribing to answers ..", name);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(assumptions);
            log.debug("Sending {}  as assumptions ..", body);
        } catch (IOException e) {
            log.error("Could not serialize assumptions ..", e);
        }

        Observable<HttpClientResponse<ServerSentEvent>> response;
        response = client.getLeft().submit(HttpClientRequest.createPost(Endpoints.ANSWER_ENDPOINT)
                                   .withContent(body));
        String rawResponse;
        rawResponse = response.doOnError(f -> log.warn("A warning ..")) // HACK, fix missing stuff & gracious shutting down
                              .flatMap(r -> r.getContent())
                              .map(f -> f.contentAsString())
                              .toBlocking()
                              .first();
        solvedInstances++;
        return objectMapper.readValue(rawResponse, Answer.class);
    }

    public void setGlobalLearnts(Observable<Clause> globalLearnts) {
        globalLearnts.filter(clause -> !seenLearntClauses.contains(clause))
                     .subscribe(cls -> {
                         log.debug("{} is sending {} as learnt clause ..",
                                   name,
                                   cls);
                         seenLearntClauses.add(cls);
                         try {
                             String message = Clauses.getBodyForStreaming(new Clauses(cls));
                             rawOutGoingClauses.onNext(message);
                         } catch (IOException e) {
                             log.error("could not serialize ..", e);
                         }
                     });
    }

    public void start(final Cnf Cnf, final String fname) {
        log.info("Starting solver connection {} ..", getName());
        try {
            client = connect(connection);
            log.info("Connection obtained for {} ..", getName());
            assertFormula(client.getLeft(), Cnf, fname);
            initIncomingFlows(client.getLeft());
            initOutgoingFlows(client.getRight());
        } catch (IOException |
                 InterruptedException |
                 TimeoutException |
                 ExecutionException e) {
            log.error("We have a problem with", e);
        }
    }

    private void initOutgoingFlows(ObservableConnection<TextWebSocketFrame,
                                                        TextWebSocketFrame> connection) {
        rawOutGoingClauses.subscribe(cls -> {
            log.debug("{} is pushing {} as learnt clause ..", name, cls);
            connection.writeAndFlush(new TextWebSocketFrame(cls));
        });
    }

    private void assertFormula(HttpClient<ByteBuf, ServerSentEvent> client, Cnf cnf, String fname) {
        Observable<HttpClientResponse<ServerSentEvent>> response;
        StringTransformer stringTransformer = new StringTransformer();

        String body = null;
        if (cnf != null) {
            try {
                body = Clauses.getBodyForStreaming(cnf.getClauses());
            } catch (IOException e) {
                log.error("Could not serialize clauses", e);
            }
            response = client.submit(HttpClientRequest.createPost(Endpoints.NEW_CLAUSES_ENDPOINT)
                             .withRawContent(body, stringTransformer));
        } else {
            body = fname;
            response = client.submit(HttpClientRequest.createPost(Endpoints.READ_FILE_ENDPOINT)
                                                      .withRawContent(body, stringTransformer));
        }
        Iterator<HttpClientResponse<ServerSentEvent>> it = response.toBlocking().getIterator();
        while (it.hasNext()) {
            HttpResponseStatus stat = it.next().getStatus();
            log.info("Recieved {} ..", stat);
        }
    }
}
