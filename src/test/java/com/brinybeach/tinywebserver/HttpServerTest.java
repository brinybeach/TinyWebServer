package com.brinybeach.tinywebserver;

import com.brinybeach.tinywebserver.handler.HttpRequestHandlerFactory;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.*;

/**
 * System level test. Start the server and make a couple of requests.
 *
 * author: bryantbunderson
 */
public class HttpServerTest extends TestCase {
    private static final Logger logger = LogManager.getLogger(HttpServerTest.class);

    public void testServerStartupAndShutdown() {

        try {
            HttpServerRunner serverRunner = new HttpServerRunner();

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future future = executorService.submit(serverRunner);

            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait(100);
            }

            assertTrue(serverRunner.isRunning());

            future.cancel(true);

            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait(3000);
            }

            assertFalse(serverRunner.isRunning());
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public void testStaticGet() {
        String requestData =
            "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        String response = runServer(requestData);

        assertTrue(response.contains("<title>Tiny Web Server</title>"));
    }

    public void testDynamicContent() {
        String requestData =
            "GET /rest/stats HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        String response = runServer(requestData);

        assertTrue(response.contains("poolsize"));
        assertTrue(response.contains("timeout"));
        assertTrue(response.contains("servertime"));
    }

    public void testBadMethod() {
        String requestData =
                "GOOT /index.html HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "User-Agent: curl/7.43.0\r\n" +
                        "Accept: */*\r\n" +
                        "\r\n";

        String response = runServer(requestData);

        assertTrue(response.contains("400"));
        assertTrue(response.contains("Bad Request"));
    }

    private String runServer(String request) {

        String response = "";

        try {
            HttpRequestHandlerFactory handlerFactory = HttpRequestHandlerFactory.getInstance();
            handlerFactory.scanPackage(HttpServerTest.class.getPackage().getName());

            HttpServerRunner serverRunner = new HttpServerRunner();

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future future = executorService.submit(serverRunner);

            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait(100);
            }

            int port = HttpServerConfig.getInstance().getPort();
            Socket clientSocket = new Socket("localhost", port);

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(request.getBytes());
            outputStream.flush();

            InputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());

            byte buffer[] = new byte[4096];
            int bytesRead = inputStream.read(buffer);

            response = new String(buffer, 0, bytesRead);

            clientSocket.close();

            future.cancel(true);

            synchronized (Thread.currentThread()) {
                while (serverRunner.isRunning())
                    Thread.currentThread().wait(100);
            }

        } catch (UnknownHostException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
        }

        return response;
    }
}
