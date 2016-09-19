package com.brinybeach.tinywebserver;

import com.brinybeach.tinywebserver.handler.HttpHandlerInstance;
import com.brinybeach.tinywebserver.handler.HttpRequestHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
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

        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            // Just keep parsing and handling requests coming in on
            // the inputStream. If the client stops sending requests
            // then the HttpRequest parser will throw an exception
            // or the socket will timeout and we will break out of
            // the loop and exit the run function and the Thread
            // will be recycled.
            while (!clientSocket.isClosed()) {

                // Bail out if the thread was interrupted
                // because the executor service was cancelled.
                if (Thread.interrupted()) {
                    logger.debug("Connection thread was interrupted");
                    break;
                }

                HttpRequestParser parser = new HttpRequestParser();

                HttpRequest request;
                HttpResponse response;

                try {
                    request = parser.parse(inputStream);

                    String method = request.getMethod();
                    String uri = request.getUri();
                    String query = request.getQuery();
                    String version = request.getVersion();

                    if (query == null) query="";
                    logger.info(String.format("%s %s%s %s", method, uri, query, version));

                    // Look for a dynamic handler for the content
                    HttpHandlerInstance handlerInstance = null;
                    HttpRequestHandlerFactory handlerFactory = HttpRequestHandlerFactory.getInstance();
                    if (handlerFactory != null) {
                        handlerInstance = handlerFactory.findHandlerMethod(request);
                    }

                    // Use the dynamic handler for the content if it exists else
                    // the default behavior is to return all requested files.
                    if (handlerInstance != null) {
                        response = handlerInstance.invokeHandler(request);
                    } else {
                        response = defaultHandler(request);
                    }

                } catch (HttpRequestParser.ParseException e) {
                    request = new HttpRequest(null, null, null, null, null, null);
                    response = new HttpResponse(400);
                }

                if (response == null) {
                    request = new HttpRequest(null, null, null, null, null, null);
                    response = new HttpResponse(500);
                }

                HttpResponseRules.apply(response, request);
                response.write(outputStream);

                if ("close".equals(response.getHeader("Connection"))) {
                    clientSocket.close();
                }
            }

            clientSocket = null;

        } catch (SocketTimeoutException e) {
            logger.debug(e);
        } catch (SocketException e) {
            logger.debug(e);
        } catch (IOException e) {
            logger.debug(e);
        } finally {
            if (clientSocket != null) {
                logger.debug("Client socket wasn't closed properly!");
                try { clientSocket.close(); } catch (Exception ignore) {}
            };
        }

        logger.debug(String.format("Client disconnected %s", clientAddress.toString()));
    }

    private HttpResponse defaultHandler(HttpRequest request) {
        HttpResponse response;

        // Default behavior is to return all requested files if they exist.
        HttpFileManager fileManager = HttpFileManager.getInstance();

        // Only support GET and HEAD by default
        if ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod())) {
            String requestUri = request.getUri();
            if ("/".equals(requestUri)) requestUri = "/index.html";

            try {
                response = new HttpResponse(200, requestUri);
                if ("HEAD".equals(request.getMethod())) {
                    // Remove body content if HEAD
                    response.setContentInputStream(null);
                }
            } catch (FileNotFoundException e) {
                response = new HttpResponse(404);
            }
        }
        else {
            // Don't support any methods other than GET and HEAD by default. A
            // RequestHandler will have to be created to handle POST and DELETE.
            response = new HttpResponse(405);
        }

        return response;
    }

}
