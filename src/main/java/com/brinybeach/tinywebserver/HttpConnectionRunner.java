package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Handle Web requests on a single socket InputStream
 * and write the response to the socket OutputStream.
 *
 * Properly handle Keep-Alive connections.
 *
 * author: bryantbunderson
 */
public class HttpConnectionRunner implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpConnectionRunner.class);

    private Socket clientSocket;

    private HttpConnectionRunner() {
    }

    public HttpConnectionRunner(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * The run method will exit when the socket OutputStream is closed.
     */
    @Override
    public void run() {
        SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
        logger.debug(String.format("Client connected %s", clientAddress.toString()));

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            // Just keep parsing and handling requests coming in on
            // the inputStream. If the client stops sending requests
            // then the HttpRequest parser will throw an exception
            // or the socket will timeout and we will break out of
            // the loop and exit the run function and the Thread
            // will be recycled.
            while (!clientSocket.isClosed()) {
                HttpRequestParser parser = new HttpRequestParser();
                HttpRequest request = parser.parse(inputStream);

                String method = request.getMethod();
                String uri = request.getUri();
                String query = request.getQuery();
                String version = request.getVersion();

                if (query == null) query="";
                logger.info(String.format("%s %s%s %s", method, uri, query, version));

                // Look for a dynamic handler for the content
                HttpRequestHandlerFactory handlerFactory = HttpRequestHandlerFactory.getInstance();
                HttpHandlerInstance handlerInstance = handlerFactory.findHandlerMethod(request);

                HttpResponse response = null;

                // Use the dynamic handler for the content if it exists else
                // the default behavior is to return all requested files.
                if (handlerInstance != null) {
                    response = handlerInstance.invokeHandler(request);
                } else {
                    // Default behavior is to return all requested files if they exist.
                    HttpFileManager fileManager = HttpFileManager.getInstance();

                    String requestUri = request.getUri();
                    if ("/".equals(requestUri)) requestUri = "/index.html";

                    if (fileManager.exists(requestUri)) {
                        response = new HttpResponse(200, requestUri);
                    } else {
                        response = new HttpResponse(404);
                    }
                }

                HttpResponseRules.apply(response, request);
                response.write(outputStream);

                if ("close".equals(response.getHeader("Connection"))) {
                    outputStream.close();
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug(e);
        } catch (SocketException e) {
            logger.debug(e);
        } catch (IOException e) {
            logger.debug(e);
        } finally {
            try { clientSocket.close(); } catch (Exception ignore) {};
        }

        logger.debug(String.format("Client disconnected %s", clientAddress.toString()));
    }
}
