package com.brinybeach.tinywebserver;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 3:48 AM
 */
@HttpController
public class TestController {

    public TestController() {
    }

    @HttpRequestHandler(method = "GET", uri = "/test")
    public HttpResponse handleRootRequest(HttpRequest request) {
        return null;
    }
}
