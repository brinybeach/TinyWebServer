package com.brinybeach.tinywebserver;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 1:44 PM
 */
public class HttpResponseTest extends TestCase {

    private static final HttpFileManager fileManager = HttpFileManager.getInstance();

    public void testSimpleGetResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());
        assertEquals("OK", response.getReason());
        assertNotNull(response.getContentInputStream());
        assertEquals(183, response.getContentLength());
        assertEquals("text/html", response.getContentType());
    }

    public void testCorrectedGetResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());
        assertEquals("OK", response.getReason());
        assertNotNull(response.getContentInputStream());
        assertEquals(183, response.getContentLength());
        assertEquals("text/html", response.getContentType());

        HttpResponseRules.apply(response, request);

        assertEquals("TinyWebServer/1.0", response.getHeader("Server"));
        assertEquals("\"a87ac353\"", response.getHeader("ETag"));
        assertEquals("close", response.getHeader("Connection"));
        assertEquals("183", response.getHeader("Content-Length"));
        assertNotNull(response.getHeader("Date"));
        assertEquals("text/html", response.getHeader("Content-Type"));
    }

    public void testKeepAliveResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "HEAD /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "Connection: Keep-Alive\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());
        assertEquals("OK", response.getReason());
        assertNotNull(response.getContentInputStream());
        assertEquals(183, response.getContentLength());
        assertEquals("text/html", response.getContentType());

        HttpResponseRules.apply(response, request);

        assertEquals("TinyWebServer/1.0", response.getHeader("Server"));
        assertEquals("\"a87ac353\"", response.getHeader("ETag"));
        assertEquals("Keep-Alive", response.getHeader("Connection"));
        assertEquals("183", response.getHeader("Content-Length"));
        assertNotNull(response.getHeader("Date"));
        assertEquals("text/html", response.getHeader("Content-Type"));
    }

    public void testCloseConnectionResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "HEAD /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "Connection: close\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());
        assertEquals("OK", response.getReason());
        assertNotNull(response.getContentInputStream());
        assertEquals(183, response.getContentLength());
        assertEquals("text/html", response.getContentType());

        HttpResponseRules.apply(response, request);

        assertEquals("TinyWebServer/1.0", response.getHeader("Server"));
        assertEquals("\"a87ac353\"", response.getHeader("ETag"));
        assertEquals("close", response.getHeader("Connection"));
        assertEquals("183", response.getHeader("Content-Length"));
        assertNotNull(response.getHeader("Date"));
        assertEquals("text/html", response.getHeader("Content-Type"));
    }

    public void testExpectHeaderResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "Expect: theworld\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(417, response.getCode());
    }

    public void testMissingHostResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(400, response.getCode());
    }

    public void testIfMatchResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "If-Match: \"a87ac353\"\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(200, response.getCode());
    }

    public void testIfMatchFailedResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "If-Match: \"14691e97\"\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(412, response.getCode());
    }

    public void testIfNonMatchGetResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "If-None-Match: \"a87ac353\"\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(304, response.getCode());
    }

    public void testIfNonMatchPutResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "PUT /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "If-None-Match: \"a87ac353\"\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(412, response.getCode());
    }

    public void testContentEncodingResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "PUT /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "Content-Encoding: gzip\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        assertEquals(415, response.getCode());
    }

    public void testSimpleWriteResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());
        assertEquals("OK", response.getReason());
        assertNotNull(response.getContentInputStream());
        assertEquals(183, response.getContentLength());
        assertEquals("text/html", response.getContentType());

        HttpResponseRules.apply(response, request);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.write(outputStream);

        String result = outputStream.toString();
        assertTrue(result.contains("HTTP/1.1 200 OK"));
        assertTrue(result.contains("ETag: \"a87ac353\""));
        assertTrue(result.contains("<title>A Test Page</title>"));
        assertTrue(result.contains("<h1>Hello, world!</h1>"));
    }

    public void testSimpleWriteErrorResponse() throws IOException, HttpRequestParser.ParseException {
        String data =
            "PUT /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "Content-Encoding: gzip\r\n" +
            "\r\n";

        ByteArrayInputStream requestStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(requestStream);

        HttpResponse response = new HttpResponse(200, request.getUri());

        assertEquals(200, response.getCode());

        HttpResponseRules.apply(response, request);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.write(outputStream);

        String result = outputStream.toString();
        assertTrue(result.contains("HTTP/1.1 415 Unsupported Media Type\r\nServer: TinyWebServer/1.0\r\nConnection: close"));
    }

}
