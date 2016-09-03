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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: bryantbunderson
 * Date: 9/2/16
 * Time: 10:08 AM
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
            logger.info("Missing server.properties file.");
        }

        port = Integer.parseInt(serverProperties.getProperty("port", "8080"));
        poolsize = Integer.parseInt(serverProperties.getProperty("poolsize", "20"));
        timeout = Integer.parseInt(serverProperties.getProperty("timeout", "5000"));
    }

    private boolean isRunning = false;
    private boolean killRequested = false;


    public HttpServerRunner() {
    }

    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void kill() {
        killRequested =  true;
    }

    @Override
    public void run() {
        logger.info(String.format("Starting server on port %d", port));
        logger.info(String.format("Connection pool size is %d", poolsize));

        ExecutorService executor = Executors.newFixedThreadPool(poolsize);

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            isRunning = true;
            killRequested = false;

            while (!killRequested) {
                // Wait for client connections on the port.
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
                executor.execute(connectionRunner);
            }

            serverSocket.close();

        } catch (Exception e) {
            logger.error(e);
        } finally {
            executor.shutdownNow();
            isRunning = false;
        }

        logger.info(String.format("Server shutdown", port));
    }
}
