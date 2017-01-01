package com.ae.sat.preprocessor.common.file;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ae on 29-12-16.
 */

@Component
public class PersistenceAccess {

    public InputStream getInputStream(String name) throws IOException {
        return new FileInputStream(new File(name));
    }
}
