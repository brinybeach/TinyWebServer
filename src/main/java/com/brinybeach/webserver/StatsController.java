package com.brinybeach.webserver;

import com.brinybeach.tinywebserver.*;
import com.brinybeach.tinywebserver.annotation.HttpController;
import com.brinybeach.tinywebserver.annotation.HttpRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The one dynamic Web content generator in the TinyWebServer. It
 * handles request to the context /rest/stats and returns a JSON document.
 *
 * author: bryantbunderson
 */
@HttpController
public class StatsController {
    private static final Logger logger = LogManager.getLogger(StatsController.class);

    private static final HttpServerConfig config = HttpServerConfig.getInstance();

    private int poolsize;
    private int timeout;

    /**
     * Load the server properties from the file rather than query the HttpServerRunner.
     */
    public StatsController() {
        this.poolsize = config.getPoolsize();
        this.timeout = config.getTimeout();
    }

    /**
     * Handle all GET requests made to the /rest/stats context and return HttpResponse
     * with a ByteArrayInputStream containing the JSON object with the server stats.
     *
     * @param request the HttpRequest to handle
     * @return the populated HttpResponse object
     */
    @HttpRequestHandler(method = "GET", uri = "/rest/stats")
    public HttpResponse handleStatsRequest(HttpRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String servertime = dateFormat.format(Calendar.getInstance().getTime());

        String contentString = String.format("{ \"poolsize\": \"%s\", \"timeout\": \"%s\", \"servertime\": \"%s\"}", poolsize, timeout, servertime);
        ByteArrayInputStream contentStream = new ByteArrayInputStream(contentString.getBytes());

        HttpFileManager fileManager = HttpFileManager.getInstance();

        HttpResponse response = new HttpResponse(200, contentStream, contentString.length(), fileManager.getContentType(".json"));
        return response;
    }
}
