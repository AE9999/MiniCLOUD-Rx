package com.ae.sat.preprocessor.common.servers.worker.controller;

import com.ae.sat.preprocessor.common.servers.worker.service.HttpService;
import com.ae.sat.preprocessor.common.servers.worker.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

/**
 * Created by ae on 14-11-16.
 */

@Controller
public class MainController implements CommandLineRunner {

    @Autowired
    private HttpService httpService;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void run(String... strings) throws Exception {
        webSocketService.start();

        httpService.startAndWait();
    }
}
