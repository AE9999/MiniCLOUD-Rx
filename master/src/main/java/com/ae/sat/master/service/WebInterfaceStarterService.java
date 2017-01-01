package com.ae.sat.master.service;

import com.ae.sat.master.service.sh.ShRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by ae on 17-12-16.
 */

@Component
public class WebInterfaceStarterService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ShRunner shRunner;

    public void startWebInterface() throws IOException {
        log.info("Starting web interface ..");

        String url = "http://localhost:8080";
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                log.info("browsing to {} ..", url);
                desktop.browse(new URI(url));
                return;
            } catch (URISyntaxException e) {
                log.warn("Could open desktop", e);
                throw new IOException(e);
            }
        }

        try {
            shRunner.executeCommandAsShArgument("gnome-open " + url);
            return;
        } catch (IOException e) {
            log.warn("Could open desktop", e);
        }

        log.warn("Browsing is not supported ..");
    }
}
