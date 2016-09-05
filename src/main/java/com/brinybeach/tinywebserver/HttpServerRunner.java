package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Accepts connections on the specified port and creates
 * a new HttpConnectionRunner to handle each connection recieved.
 *
 * Uses settings from the server.properties file.
 *
 * Default settings are:
 *
 * port=8080
 * poolsize=20
 * timeout=5000
 *
 * author: bryantbunderson
 */
public class HttpServerRunner implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpServerRunner.class);

    private static int port;
    private static int poolsize;
    private static int timeout;
    {
        Properties serverProperties = new Properties();
        try {
            serverProperties.load(new FileInputStream("server.properties"));
        } catch (Exception e) {
            logger.warn("Missing server.properties file.");
        }

        port = Integer.parseInt(serverProperties.getProperty("port", "8080"));
        poolsize = Integer.parseInt(serverProperties.getProperty("poolsize", "20"));
        timeout = Integer.parseInt(serverProperties.getProperty("timeout", "5000"));
    }

    private boolean isRunning = false;
    private boolean killRequested = false;

    /**
     * A new HttpServerRunner
     */
    public HttpServerRunner() {
    }

    /**
     * @return the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return true if the server is running and accepting connections
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Request that the server stop
     */
    public void kill() {
        killRequested =  true;
    }

    /**
     * This method accepts connection from Web clients until
     * the kill() method is called or it is interrupted.
     */
    @Override
    public void run() {
        logger.info(String.format("Starting server on port %d", port));
        logger.info(String.format("Connection pool size is %d", poolsize));
        logger.info(String.format("Connection timeout is %d", timeout));

        ExecutorService executorService = Executors.newFixedThreadPool(poolsize);
        ServerSocket serverSocket = null;

        isRunning = true;

        try {
            serverSocket = new ServerSocket(port);

            killRequested = false;
            while (!killRequested) {
                // Wait for client connections on the port.
                logger.debug("Waiting for client connection...");
                Socket clientSocket = serverSocket.accept();

                // Timeout client connections so that badly behaved Web clients
                // don't plug up our thread pool by not disconnecting properly.
                clientSocket.setSoTimeout(timeout);

                // Create a new clientRunner to handle the connection. Add the runner to
                // the executor service which wraps it in a thread from the pool. If the
                // thread pool is full then the runners are queued up until one comes
                // available. It's good to use a shortish timeout on the clientSocket so
                // that the thread pool isn't overwhelmed by dead connections that the
                // client doesn't close.
                HttpConnectionRunner connectionRunner = new HttpConnectionRunner(clientSocket);
                executorService.execute(connectionRunner);
            }

        } catch (Exception e) {
            logger.error(e);
        } finally {
            try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignore) {}
            executorService.shutdownNow();
        }

        isRunning = false;

        logger.info(String.format("Server shutdown", port));
    }
}
