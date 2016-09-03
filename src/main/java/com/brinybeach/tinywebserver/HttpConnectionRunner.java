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
 * User: bryantbunderson
 * Date: 9/2/16
 * Time: 10:07 AM
 */
public class HttpConnectionRunner implements Runnable {
    private static final Logger logger = LogManager.getLogger(HttpConnectionRunner.class);

    private Socket clientSocket;

    private HttpConnectionRunner() {
    }

    public HttpConnectionRunner(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

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
                logger.debug(String.format("REQUEST: %s %s%s %s", method, uri, query, version));

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
            }
        } catch (SocketTimeoutException e) {
            // logger.error(e);
        } catch (SocketException e) {
            logger.error(e);
        } catch (IOException e) {
            // logger.error(e);
        } finally {
            if (inputStream != null) try { inputStream.close(); } catch (Exception ignore) {}
            if (outputStream != null) try { outputStream.close(); } catch (Exception ignore) {}
        }

        logger.debug(String.format("Client disconnected %s", clientAddress.toString()));
    }
}
