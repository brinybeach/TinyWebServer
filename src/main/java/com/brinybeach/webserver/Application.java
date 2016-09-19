package com.brinybeach.webserver;

import com.brinybeach.tinywebserver.HttpServerRunner;
import com.brinybeach.tinywebserver.handler.HttpRequestHandlerFactory;
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
        HttpRequestHandlerFactory handlerFactory = HttpRequestHandlerFactory.getInstance();
        handlerFactory.scanPackage(Application.class.getPackage().getName());

        HttpServerRunner serverRunner = new HttpServerRunner();
        serverRunner.run();
    }
}
