package com.brinybeach.tinywebserver;

import java.util.HashMap;
import java.util.Map;

/**
 * User: bryantbunderson
 * Date: 8/31/16
 *
 * HttpRequests are constant; their values cannot be
 * changed after they are created. Because HttpRequest
 * objects are immutable they can be shared.
 */
public class HttpRequest {

    private String method;
    private String uri;
    private String query;
    private String version;
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;

    private boolean isValid;


    private HttpRequest() {
    }

    public HttpRequest(String method, String uri, String query, String version, Map<String, String> headers, String body) {
        this(method, uri, query, version, headers, body, true);
    }

    public HttpRequest(String method, String uri, String query, String version, Map<String, String> headers, String body, boolean isValid) {
        this.method = method;
        this.uri = uri;
        this.query = query;
        this.version = version;
        this.headers.putAll(headers);
        this.body = body;
        this.isValid = isValid;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getQuery() {
        return query;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders(String name) {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return body;
    }

    public boolean existsMethod() {
        return (method != null);
    }

    public boolean existsUri() {
        return (uri != null);
    }

    public boolean existsQuery() {
        return (uri != null);
    }

    public boolean existsVersion() {
        return (version != null);
    }

    public boolean existsHeader(String name) {
        return headers.containsKey(name);
    }

    public boolean existsBody() {
        return (body != null);
    }
}
