package com.ae.sat.master.service.connection;

import java.io.IOException;

/**
 * Created by ae on 17-12-16.
 */
public interface Connection {

    int httpPort();

    int wsPort();

    String host();

    void close() throws IOException;

}
