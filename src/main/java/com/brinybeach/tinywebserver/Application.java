package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main entry point for the TinyWebServer application.
 *
 * author: bryantbunderson
 */
public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        HttpServerRunner serverRunner = new HttpServerRunner();
        serverRunner.run();
    }
}
