package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.server.ExportException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    private static final HttpServerConfig config = HttpServerConfig.getInstance();

    private int port;
    private int poolsize;
    private int timeout;

    private AtomicBoolean runningState = new AtomicBoolean(false);

    /**
     * A new HttpServerRunner
     */
    public HttpServerRunner() {
        this.port = config.getPort();
        this.poolsize = config.getPoolsize();
        this.timeout = config.getTimeout();
    }

    public boolean isRunning() {
        return runningState.get();
    }

    /**
     * This method accepts connection from Web clients until
     * the kill() method is called or it is interrupted.
     */
    @Override
    public void run() {
        logger.info(String.format("Connection pool size is %d", poolsize));
        logger.info(String.format("Connection timeout is %d", timeout));

        ExecutorService executorService = Executors.newFixedThreadPool(poolsize);
        ServerSocket serverSocket = null;

        runningState.set(true);

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(2000);

            logger.info(String.format("Server started on port %d", port));

            // Wait for client connections on the port.
            logger.debug("Waiting for client connection...");
            while (!Thread.interrupted()) {
                try {
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

                } catch (SocketTimeoutException ignore) {
                    // This was caused by the server socket accept timeout so ignored it
                    // and continue as long as the server runner thread is not interrupted.
                }
            }

            serverSocket.close();
            serverSocket = null;

            try {
                executorService.shutdownNow();
                executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Client connections did not close down properly!");
            }

            logger.info(String.format("Server shutdown", port));

        } catch (IOException e) {
            logger.error(e);
        } finally {
            if (serverSocket != null) {
                logger.error("The server socket wasn't closed properly!");
                try { serverSocket.close(); } catch (Exception ignore) {}
            }
        }

        runningState.set(false);
    }

}
