package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 3:48 AM
 */
@HttpController
public class StatsController {
    private static final Logger logger = LogManager.getLogger(StatsController.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    private int poolsize;
    private int timeout;

    public StatsController() {
        Properties serverProperties = new Properties();
        try {
            // Load the server properties file. If the load fails then
            // the getPropertiy() calls will just use the default values.
            serverProperties.load(new FileInputStream("server.properties"));
        } catch (Exception ignore) {}

        poolsize = Integer.parseInt(serverProperties.getProperty("poolsize", "20"));
        timeout = Integer.parseInt(serverProperties.getProperty("timeout", "5000"));
    }

    @HttpRequestHandler(method = "GET", uri = "/rest/stats")
    public HttpResponse handleStatsRequest(HttpRequest request) {
        String servertime = dateFormat.format(Calendar.getInstance().getTime());

        String contentString = String.format("{ \"poolsize\": \"%s\", \"timeout\": \"%s\", \"servertime\": \"%s\"}", poolsize, timeout, servertime);
        ByteArrayInputStream contentStream = new ByteArrayInputStream(contentString.getBytes());

        HttpFileManager fileManager = HttpFileManager.getInstance();

        HttpResponse response = new HttpResponse(200, contentStream, contentString.length(), fileManager.getContentType(".json"));
        return response;
    }
}
