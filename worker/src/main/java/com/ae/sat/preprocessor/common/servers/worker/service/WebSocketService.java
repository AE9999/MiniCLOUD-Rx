package com.ae.sat.preprocessor.common.servers.worker.service;

import com.ae.sat.preprocessor.common.model.formulas.Clause;
import com.ae.sat.preprocessor.common.model.formulas.Clauses;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.RxServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import rx.Observable;
import rx.observables.StringObservable;

import java.io.IOException;

/**
 * Created by ae on 4-12-16.
 */

@Component
public class WebSocketService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper = new ObjectMapper();

    RxServer<WebSocketFrame, WebSocketFrame> server;

    @Value("${webSocketPort}")
    private int port;

    @Autowired
    private SolverService solverService;

    private RxServer<WebSocketFrame, WebSocketFrame> createServer() {
        RxServer<WebSocketFrame, WebSocketFrame> server;
        ConnectionHandler<WebSocketFrame, WebSocketFrame> connectionHandler;
        connectionHandler = (connection) -> {
            Observable<String> input;
            input = connection.getInput().map(
                wsFrame -> {
                    TextWebSocketFrame textFrame = (TextWebSocketFrame) wsFrame;
                    log.debug("Recieved {} as over websockets ..", textFrame.text());
                    if (textFrame.text().equals("PING")) {
                        log.debug("Sending a pong response back ..");
                        connection.writeAndFlush(new TextWebSocketFrame("PONG"));
                        return "";
                    } else {
                        return textFrame.text();
                    }
                }
            );
            input.doOnError(e -> log.error("Could not reply due to ", e));

            Observable<Void> doStuff;
            doStuff = StringObservable.split(input, Clauses.SEPERATOR)
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
                                    });
            return doStuff;
        };

        server = RxNetty.newWebSocketServerBuilder(port, connectionHandler)
                        .enableWireLogging(LogLevel.DEBUG)
                        .build();
        return server;
    }

    public void start() {
        log.info("Starting Websocket server ..");
        server = createServer();
        server.start();
    }
}
