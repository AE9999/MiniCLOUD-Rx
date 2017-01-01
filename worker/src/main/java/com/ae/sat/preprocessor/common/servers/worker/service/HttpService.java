package com.ae.sat.preprocessor.common.servers.worker.service;

import com.ae.sat.preprocessor.common.model.formulas.Answer;
import com.ae.sat.preprocessor.common.model.formulas.Assumptions;
import com.ae.sat.preprocessor.common.model.formulas.Clause;
import com.ae.sat.preprocessor.common.model.Endpoints;
import com.ae.sat.preprocessor.common.model.formulas.Clauses;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.protocol.http.sse.ServerSentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import rx.Notification;
import rx.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.observables.StringObservable;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by ae on 21-5-16.
 */
@Service
public class HttpService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${httpPort}")
    private int port;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SolverService solverService;

    private HttpServer<ByteBuf, ServerSentEvent> server;

    private HttpServer<ByteBuf, ServerSentEvent> createServer() {
        RequestHandler<ByteBuf, ServerSentEvent> requestHandler;
        requestHandler = (request, response) -> {
            try {
                log.info(String.format("Recieved a connection request %s..", request.getPath()));
                switch (request.getPath()) {
                    case Endpoints.READ_FILE_ENDPOINT:
                        return readFile(response);
                    case Endpoints.STATS_ENDPOINT:
                        return getStats(response);
                    case Endpoints.LEARNTS_ENDPOINT:
                        return getLearnts(response);
                    case Endpoints.NEW_CLAUSES_ENDPOINT:
                        return addClauses(request, response);
                    case Endpoints.READY_ENDPOINT:
                        return ready(response);
                    case Endpoints.ANSWER_ENDPOINT:
                    default:
                        return getAnswer(request, response);
                }
            } catch (Exception e) {
                log.error("Could not complete request", e);
                throw new RuntimeException(e);
            }
        };

        PipelineConfigurator<HttpServerRequest<ByteBuf>,
                             HttpServerResponse<ServerSentEvent>> configurator;

        configurator = PipelineConfigurators.serveSseConfigurator();

        HttpServer<ByteBuf, ServerSentEvent> server;
        server = RxNetty.createHttpServer(port, requestHandler, configurator)
                        .withErrorHandler(error -> {
                            log.error("Got an error:", error);
                            return Observable.empty();
                        });

        log.info("HTTP Server Sent Events server started...");
        return server;
    }

    private Observable<Void> readFile(HttpServerResponse<ServerSentEvent> response) {
        return null;
    }

    private Observable<Void> ready(HttpServerResponse<ServerSentEvent> response) {
        response.setStatus(HttpResponseStatus.OK);
        return response.close();
    }

    private ServerSentEvent translateObjectToSSE(Object object) {
        try {
            String  body = objectMapper.writeValueAsString(object);
            return new ServerSentEvent(Unpooled.copiedBuffer(body.getBytes()));
        } catch (JsonProcessingException e) {
            log.error("Could not parse body", e);
            return null;
        }
    }

    private Observable<Void> doStuff(Observable<Notification<Void>> observable) {
        return observable.takeWhile(notification -> {
            if (notification.isOnError()) {
                log.error("Write to client failed, stopping response sending.",
                        notification.getThrowable());
            }
            return !notification.isOnError();
        }).map(f -> null);
    }

    private Observable<Void> addClauses(HttpServerRequest<ByteBuf> request,
                                        HttpServerResponse<ServerSentEvent> response) {
        Observable<String> input = request.getContent().map(c -> c.toString(Charset.defaultCharset()));
        Observable<Void> doStuff = StringObservable.split(input, Clauses.SEPERATOR)
                        .filter(r -> !StringUtils.isEmpty(r))
                        .map(r -> {
                                    log.debug("Read {} as clause ..", r);
                                    Clause cls = null;
                                    try {
                                        cls = objectMapper.readValue(r, Clause.class);
                                        solverService.addClause(cls);
                                    } catch (IOException e) {
                                        log.error("could not parse clause.", e);
                                    }
                                    return null;
                                  }
                        );
        return doStuff.doOnCompleted(() -> {
            response.setStatus(HttpResponseStatus.OK);
            response.close();
        });

    }

    private Observable<Void> getStats(final HttpServerResponse<ServerSentEvent> response) {
        return doStuff(solverService.stats()
                                    .flatMap(stats -> {
                                        log.info(String.format("Writing %s ..", stats));
                                        return response.writeAndFlush(translateObjectToSSE(stats));
                                    })
                                    .materialize());
    }

    private Observable<Void> getLearnts(final HttpServerResponse<ServerSentEvent> response) {
        return doStuff(solverService.learnts()
                                    .flatMap(learnts -> {
                                        log.info(String.format("Writing %s ..", learnts));
                                        return response.writeAndFlush(translateObjectToSSE(learnts));
                                    })
                                    .materialize());
    }

    private Observable<Void> getAnswer(final HttpServerRequest<ByteBuf> request,
                                       final HttpServerResponse<ServerSentEvent> response) {
        return doStuff(request.getContent()
                              .map(rawContent -> rawContent.toString(Charset.defaultCharset()))
                              .map(stringContent -> {
                                    try {
                                      return objectMapper.readValue(stringContent, Assumptions.class);
                                    } catch (IOException e) {
                                      log.error("Could not read assumptions ..", e);
                                      return null;
                                    }
                                })
                              .flatMap(assumptions -> {
                                    if (assumptions == null) {
                                        return Observable.just(Answer.UNKOWN);
                                    }
                                    return solverService.solve(assumptions);
                                })
                              .flatMap(answer -> {
                                    log.info(String.format("Writing %s ..", answer));
                                    return response.writeAndFlush(translateObjectToSSE(answer));
                                })
                              .materialize());
    }

    @PostConstruct
    private void init() {
        server = createServer();
    }

    public void startAndWait() {
        log.info("Starting http server   ..");
        server.startAndWait();
    }

}
