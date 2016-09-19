package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * User: Bryant Bunderson
 * Date: 9/18/16
 */
public class HttpServerConfig {
    private static final Logger logger = LogManager.getLogger(HttpServerConfig.class);

    private static final HttpServerConfig instance = new HttpServerConfig();

    private final int port;
    private final int poolsize;
    private final int timeout;
    private final String directory;

    public static HttpServerConfig getInstance() {
        return instance;
    }

    private HttpServerConfig() {
        Properties serverProperties = new Properties();
        try {
            serverProperties.load(new FileInputStream("server.properties"));
        } catch (Exception e) {
            logger.warn("Bad or missing server.properties file.");
        }

        port = Integer.parseInt(serverProperties.getProperty("port", "8080"));
        poolsize = Integer.parseInt(serverProperties.getProperty("poolsize", "20"));
        timeout = Integer.parseInt(serverProperties.getProperty("timeout", "5000"));
        directory = serverProperties.getProperty("directory", "www");
    }

    public int getPort() {
        return port;
    }

    public int getPoolsize() {
        return poolsize;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getDirectory() {
        return directory;
    }
}
