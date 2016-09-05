package com.brinybeach.tinywebserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrap the parsed request attributes received from the
 * Web client. HttpRequests are created by the HttpRequestParser.
 *
 * HttpRequests are constant; their values cannot be
 * changed after they are created. Because HttpRequest
 * objects are immutable they can be shared.
 *
 * author: bryantbunderson
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

    /**
     * Create the HttpRequest from individual parsed attributes.
     *
     * @param method 5.1.1 Method
     * @param uri 5.1.2 Request-URI (path part)
     * @param query 5.1.2 Request-URI (query part)
     * @param version 3.1 HTTP Version
     * @param headers 5.3 Request Header Fields
     * @param body 4.3 Message Body
     */
    public HttpRequest(String method, String uri, String query, String version, Map<String, String> headers, String body) {
        this(method, uri, query, version, headers, body, true);
    }

    /**
     * Create the HttpRequest from individual parsed attributes.
     *
     * @param method 5.1.1 Method
     * @param uri 5.1.2 Request-URI (path part)
     * @param query 5.1.2 Request-URI (query part)
     * @param version 3.1 HTTP Version
     * @param headers 5.3 Request Header Fields
     * @param body 4.3 Message Body
     * @param isValid is false if an error occurred while parsing the request
     */
    public HttpRequest(String method, String uri, String query, String version, Map<String, String> headers, String body, boolean isValid) {
        this.method = method;
        this.uri = uri;
        this.query = query;
        this.version = version;
        this.headers.putAll(headers);
        this.body = body;
        this.isValid = isValid;
    }

    /**
     * @return true if the HttpRequest was parsed correctly.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * @return the HTTP method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the HTTP URI without the query
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the HTTP URI query part
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return the HTTP version HTTP/1.0 or HTTP/1.1
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param name the header field name
     * @return the HTTP header field value that matches the field name
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * @return the HTTP body contents
     */
    public String getBody() {
        return body;
    }

    /**
     * @return true if the HTTP method exists
     */
    public boolean existsMethod() {
        return (method != null);
    }

    /**
     * @return true if the URI (path part) exists
     */
    public boolean existsUri() {
        return (uri != null);
    }

    /**
     * @return true if the URI (query part) exists
     */
    public boolean existsQuery() {
        return (uri != null);
    }

    /**
     * @return true if the HTTP version exists
     */
    public boolean existsVersion() {
        return (version != null);
    }

    /**
     * @param name the header field name
     * @return true if the specified HTTP header field exists
     */
    public boolean existsHeader(String name) {
        return headers.containsKey(name);
    }

    /**
     * @return true if the body exists
     */
    public boolean existsBody() {
        return (body != null);
    }
}
