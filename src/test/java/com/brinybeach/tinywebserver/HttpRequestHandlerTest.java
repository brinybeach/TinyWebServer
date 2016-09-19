package com.brinybeach.tinywebserver;

import com.brinybeach.tinywebserver.handler.HttpHandlerInstance;
import com.brinybeach.tinywebserver.handler.HttpRequestHandlerFactory;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 2:19 AM
 */
public class HttpRequestHandlerTest extends TestCase {

    public void testSimpleGetHandler() throws IOException, HttpRequestParser.ParseException {
        String data =
            "GET /test/test.html HTTP/1.1\r\n" +
            "Host: localhost:8080\r\n" +
            "User-Agent: curl/7.43.0\r\n" +
            "Accept: */*\r\n" +
            "\r\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());

        HttpRequestParser parser = new HttpRequestParser();
        HttpRequest request = parser.parse(inputStream);

        HttpRequestHandlerFactory handlerFactory = HttpRequestHandlerFactory.getInstance();
        handlerFactory.scanPackage(this.getClass().getPackage().getName());

        HttpHandlerInstance handlerInstance = handlerFactory.findHandlerMethod(request);
        assertNotNull(handlerInstance);
        assertTrue(handlerInstance.getObject() instanceof TestController);

        HttpResponse response = handlerInstance.invokeHandler(request);
        assertNull(response);
    }
}
