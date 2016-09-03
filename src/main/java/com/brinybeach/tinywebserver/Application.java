package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: bryantbunderson
 * Date: 8/31/16
 * Time: 7:56 PM
 */
public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            Thread serverThread = new Thread(new HttpServerRunner());
            serverThread.start();

            // Don't exit until the server thread dies.
            serverThread.join();
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }
}
