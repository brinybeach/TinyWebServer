package com.brinybeach.tinywebserver;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * User: bryantbunderson
 * Date: 9/2/16
 * Time: 3:28 PM
 */
public class HttpServerTest extends TestCase {
    private static final Logger logger = LogManager.getLogger(HttpServerTest.class);

    public void testStaticGet() {
        String requestData =
            "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        logger.debug("testStaticGet()");

        HttpServerRunner serverRunner = new HttpServerRunner();

        Thread serverThread = new Thread(new HttpServerRunner());
        serverThread.start();

        String response = "";

        try {
            // Wait for the server to start
            for (int i = 0; i < 20; i++) {
                if (!serverRunner.isRunning()) {
                    synchronized (this) {
                        wait(100);
                    }
                }
            }

            Socket clientSocket = new Socket("localhost", serverRunner.getPort());

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(requestData.getBytes());
            outputStream.flush();

            InputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());

            byte buffer[] = new byte[4096];
            int bytesRead = inputStream.read(buffer);

            response = new String(buffer, 0, bytesRead);

            inputStream.close();
            outputStream.close();

            clientSocket.close();

            serverRunner.kill();

        } catch (InterruptedException e) {
            logger.error(e);
        } catch (UnknownHostException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } finally {
            serverRunner.kill();
            serverThread.interrupt();
        }

        assertTrue(response.contains("<title>Tiny Web Server</title>"));
    }

    public void testDynamicContent() {
        String requestData =
            "GET /rest/stats HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        logger.debug("testStaticGet()");

        HttpServerRunner serverRunner = new HttpServerRunner();

        Thread serverThread = new Thread(new HttpServerRunner());
        serverThread.start();

        String response = "";

        try {
            // Wait for the server to start
            for (int i = 0; i < 20; i++) {
                if (!serverRunner.isRunning()) {
                    synchronized (this) {
                        wait(100);
                    }
                }
            }

            Socket clientSocket = new Socket("localhost", serverRunner.getPort());

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(requestData.getBytes());
            outputStream.flush();

            InputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());

            byte buffer[] = new byte[4096];
            int bytesRead = inputStream.read(buffer);

            response = new String(buffer, 0, bytesRead);

            inputStream.close();
            outputStream.close();

            clientSocket.close();

            serverRunner.kill();

        } catch (InterruptedException e) {
            logger.error(e);
        } catch (UnknownHostException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } finally {
            serverRunner.kill();
            serverThread.interrupt();
        }

        assertTrue(response.contains("poolsize"));
        assertTrue(response.contains("timeout"));
        assertTrue(response.contains("servertime"));
    }
}
