package com.brinybeach.tinywebserver;

import com.brinybeach.tinywebserver.annotation.HttpController;
import com.brinybeach.tinywebserver.annotation.HttpRequestHandler;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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

    @HttpRequestHandler(method = "GET", uri = "/rest/stats")
    public HttpResponse handleStatsRequest(HttpRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String servertime = dateFormat.format(Calendar.getInstance().getTime());

        String contentString = String.format("{ \"poolsize\": \"%s\", \"timeout\": \"%s\", \"servertime\": \"%s\"}", 50, 5000, servertime);
        ByteArrayInputStream contentStream = new ByteArrayInputStream(contentString.getBytes());

        HttpFileManager fileManager = HttpFileManager.getInstance();

        HttpResponse response = new HttpResponse(200, contentStream, contentString.length(), fileManager.getContentType(".json"));
        return response;
    }

}
