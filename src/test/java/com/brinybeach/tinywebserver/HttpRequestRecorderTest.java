package com.brinybeach.tinywebserver;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * User: bryantbunderson
 * Date: 9/2/16
 * Time: 10:12 AM
 */
public class HttpRequestRecorderTest extends TestCase {
    private static final Logger logger = LogManager.getLogger(HttpRequestRecorderTest.class);

    private static final int port = 8080;
    private static final int timeout = 5000;

    /**
     * This test was used to record Web requests to better understand the
     * HTTP RFC-2616. It isn't a true test case but rather an easy place
     * to put this code so that is doesn't clutter up the main source tree.
     */
    public void testRequestRecorder() {
        // recordWebRequest();
        assertTrue(true);
    }

    private void recordWebRequest() {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            logger.info(String.format("Server started on port %d", port));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(timeout);

                SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
                logger.debug(String.format("Client connected %s", clientAddress.toString()));

                BufferedInputStream inputStream = null;
                BufferedOutputStream outputStream = null;

                try {
                    inputStream = new BufferedInputStream(clientSocket.getInputStream());
                    outputStream = new BufferedOutputStream(clientSocket.getOutputStream());

                    while (!clientSocket.isClosed()) {

                        // Read the request and print it to the console
                        byte requestBuffer[] = new byte[8192];
                        int bytesRead = inputStream.read(requestBuffer);

                        String requestString = new String(requestBuffer, 0, bytesRead);
                        System.out.println(requestString);

                        FileOutputStream captureStream = new FileOutputStream("capture.txt");
                        captureStream.write(requestBuffer, 0, bytesRead);
                        captureStream.close();

                        logger.info(new String(requestBuffer, 0, bytesRead));

                        // Always respond with a 500 (internal server error)
                        outputStream.write("HTTP/1.1 503 Service Unavailable\r\nConnection: close\r\n\r\n".getBytes());
                        outputStream.flush();

                    }
                } catch (SocketTimeoutException e) {
                    logger.error(e);
                } catch (SocketException e) {
                    logger.error(e);
                } catch (IOException e) {
                    logger.error(e);
                } finally {
                    if (inputStream != null) try { inputStream.close(); } catch (Exception ignore) {}
                    if (outputStream != null) try { outputStream.close(); } catch (Exception ignore) {}
                }

                logger.debug(String.format("Client disconnected %s", clientAddress.toString()));
            }

        } catch (IOException e) {
            logger.error(e);
        } finally {
            try { serverSocket.close(); } catch(Exception ignore) {}
        }

    }
}
