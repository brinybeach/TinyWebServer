package com.brinybeach.tinywebserver;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * User: bryantbunderson
 * Date: 8/31/16
 */
public class HttpRequestParserTest extends TestCase {

    public void testCurlRequest() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(inputStream);

        assertTrue(request.existsMethod());
        assertEquals("GET", request.getMethod());

        assertTrue(request.existsUri());
        assertEquals("/index.html", request.getUri());

        assertTrue(request.existsVersion());
        assertEquals("HTTP/1.1", request.getVersion());

        assertTrue(request.existsHeader("Host"));
        assertEquals("localhost:8080", request.getHeader("Host"));

        assertTrue(request.existsHeader("User-Agent"));
        assertEquals("curl/7.43.0", request.getHeader("User-Agent"));

        assertTrue(request.existsHeader("Accept"));
        assertEquals("*/*", request.getHeader("Accept"));
    }

    public void testSafariRequest() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET / HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Accept-Encoding: gzip, deflate\r\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/601.7.7 (KHTML, like Gecko) Version/9.1.2 Safari/601.7.7\r\n" +
            "Accept-Language: en-us\r\n" +
            "DNT: 1\r\n" +
            "Connection: keep-alive\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(inputStream);

        assertTrue(request.existsMethod());
        assertEquals("GET", request.getMethod());

        assertTrue(request.existsUri());
        assertEquals("/", request.getUri());

        assertTrue(request.existsVersion());
        assertEquals("HTTP/1.1", request.getVersion());

        assertTrue(request.existsHeader("Host"));
        assertEquals("localhost:8080", request.getHeader("Host"));

        assertTrue(request.existsHeader("Accept-Encoding"));
        assertEquals("gzip, deflate", request.getHeader("Accept-Encoding"));

        assertTrue(request.existsHeader("Accept"));
        assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8", request.getHeader("Accept"));

        assertTrue(request.existsHeader("User-Agent"));
        assertEquals("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/601.7.7 (KHTML, like Gecko) Version/9.1.2 Safari/601.7.7", request.getHeader("User-Agent"));

        assertTrue(request.existsHeader("Accept-Language"));
        assertEquals("en-us", request.getHeader("Accept-Language"));

        assertTrue(request.existsHeader("DNT"));
        assertEquals("1", request.getHeader("DNT"));

        assertTrue(request.existsHeader("Connection"));
        assertEquals("keep-alive", request.getHeader("Connection"));
    }

    public void testChromeRequest() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "Connection: keep-alive\r\n" +
            "Cache-Control: max-age=0\r\n" +
            "Upgrade-Insecure-Requests: 1\r\n" +
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\r\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
            "Accept-Encoding: gzip, deflate, sdch\r\n" +
            "Accept-Language: en-US,en;q=0.8\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(inputStream);

        assertTrue(request.existsMethod());
        assertEquals("GET", request.getMethod());

        assertTrue(request.existsUri());
        assertEquals("/index.html", request.getUri());

        assertTrue(request.existsVersion());
        assertEquals("HTTP/1.1", request.getVersion());

        assertTrue(request.existsHeader("Host"));
        assertEquals("localhost:8080", request.getHeader("Host"));

        assertTrue(request.existsHeader("Connection"));
        assertEquals("keep-alive", request.getHeader("Connection"));

        assertTrue(request.existsHeader("Cache-Control"));
        assertEquals("max-age=0", request.getHeader("Cache-Control"));

        assertTrue(request.existsHeader("Upgrade-Insecure-Requests"));
        assertEquals("1", request.getHeader("Upgrade-Insecure-Requests"));

        assertTrue(request.existsHeader("User-Agent"));
        assertEquals("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36", request.getHeader("User-Agent"));

        assertTrue(request.existsHeader("Accept"));
        assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8", request.getHeader("Accept"));

        assertTrue(request.existsHeader("Accept-Encoding"));
        assertEquals("gzip, deflate, sdch", request.getHeader("Accept-Encoding"));

        assertTrue(request.existsHeader("Accept-Language"));
        assertEquals("en-US,en;q=0.8", request.getHeader("Accept-Language"));
    }

    public void testQuery() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /my/query?p1=1&p2=2 HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(inputStream);

        assertTrue(request.existsMethod());
        assertEquals("GET", request.getMethod());

        assertTrue(request.existsUri());
        assertEquals("/my/query", request.getUri());

        assertTrue(request.existsQuery());
        assertEquals("?p1=1&p2=2", request.getQuery());

        assertTrue(request.existsVersion());
        assertEquals("HTTP/1.1", request.getVersion());

        assertTrue(request.existsHeader("Host"));
        assertEquals("localhost:8080", request.getHeader("Host"));

        assertTrue(request.existsHeader("User-Agent"));
        assertEquals("curl/7.43.0", request.getHeader("User-Agent"));

        assertTrue(request.existsHeader("Accept"));
        assertEquals("*/*", request.getHeader("Accept"));
    }

    public void testBadMethod() throws IOException {
        String data =
            "GOOT /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        try {
            HttpRequestParser parser = new HttpRequestParser();
            HttpRequest request = parser.parse(inputStream);

            assertFalse("Expected ParseException to be thrown", true);
        } catch (HttpRequestParser.ParseException e) {
        }
    }

    public void testBadUriNotAbsolutePath() throws IOException {
        String data =
                "GET index.html HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "User-Agent: curl/7.43.0\r\n" +
                        "Accept: */*\r\n" +
                        "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        try {
            HttpRequestParser parser = new HttpRequestParser();
            HttpRequest request = parser.parse(inputStream);

            assertFalse("Expected ParseException to be thrown", true);
        } catch (HttpRequestParser.ParseException e) {
        }
    }


    public void testBadUriInvalidCharacters() throws IOException {
        String data =
            "GET /inde^!&@#x.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        try {
            HttpRequestParser parser = new HttpRequestParser();
            HttpRequest request = parser.parse(inputStream);

            assertFalse("Expected ParseException to be thrown", true);
        } catch (HttpRequestParser.ParseException e) {
        }
    }

    public void testBadHeaderCharacters() throws IOException {
        String data =
            "GET /index.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-{Agent}: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        try {
            HttpRequestParser parser = new HttpRequestParser();
            HttpRequest request = parser.parse(inputStream);

            assertFalse("Expected ParseException to be thrown", true);
        } catch (HttpRequestParser.ParseException e) {
        }
    }

    public void testDynamicRequest() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /rest/stats HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(inputStream);

        assertTrue(request.existsMethod());
        assertEquals("GET", request.getMethod());

        assertTrue(request.existsUri());
        assertEquals("/rest/stats", request.getUri());

        assertTrue(request.existsVersion());
        assertEquals("HTTP/1.1", request.getVersion());

        assertTrue(request.existsHeader("Host"));
        assertEquals("localhost:8080", request.getHeader("Host"));

        assertTrue(request.existsHeader("User-Agent"));
        assertEquals("curl/7.43.0", request.getHeader("User-Agent"));

        assertTrue(request.existsHeader("Accept"));
        assertEquals("*/*", request.getHeader("Accept"));
    }
}
