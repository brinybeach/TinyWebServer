package com.brinybeach.tinywebserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * User: bryantbunderson
 * Date: 9/2/16
 * Time: 12:38 AM
 */
public class HttpResponseRules {

    public static void apply(HttpResponse response, HttpRequest request) {
        String value;

        // First thing check for an invalid request
        // before applying rules found in the RFC
        if (!request.isValid()) {
            setErrorCode(400, response, request);
            return;
        }

        //
        // Now apply rules that were decided upon during the research
        // phase of the project. These rules are based on header information.
        //

        // *** General Headers (ignore those not listed below)
        //
        // Cache-Control ; Section 14.9
        // don't support caching so return a "no-cache"

        // Connection ; Section 14.10
        // Must support this and handle and send a "close" if the client requests a close.
        // Must send a close if the request Connection header is missing or is not Keep-Alive.
        // Should send a Keep-Alive if the request contained a keep alive.
        value = request.getHeader("Connection");
        if ("close".equalsIgnoreCase(request.getHeader("Connection"))) {
            response.putHeader("Connection", "close");
        }
        if (value == null) {
            response.putHeader("Connection", "close");
        }
        if ("Keep-Alive".equalsIgnoreCase(value)) {
            response.putHeader("Connection", "Keep-Alive");
        }

        // Date ; Section 14.18
        // must support this and send a HTTP date in response
        response.putHeader("Date", dateFormat.format(Calendar.getInstance().getTime()));

        // *** Request Headers (only support those listed below)
        //
        // Accept ; Section 14.1
        // Check this and send response 406 (not acceptable) if needed

        // Accept-Charset ; Section 14.2
        // Check this and send response 406 (not acceptable) if needed

        // Accept-Encoding ; Section 14.3
        // Do not compress anything so ignore this header

        // Authorization ; Section 14.8
        // Do not support authentication

        // Expect ; Section 14.20
        // Always respond with a 417 (Expectation Failed)
        if (request.existsHeader("Expect")) {
            setErrorCode(417, response, request);
            return;
        }

        // Host ; Section 14.23
        // Must respond with a 400 (Bad Request) to any HTTP/1.1
        // request message which lacks a Host header field.
        if ("HTTP/1.1".equals(request.getVersion()) && !request.existsHeader("Host")) {
            setErrorCode(400, response, request);
            return;
        }

        // If-Match ; Section 14.24
        // Must return a 412 (Precondition Failed) response if
        // this ETag check fails. This means that responses must
        // include ETags like Etag: "686897696a7c876b7e1".
        value = request.getHeader("If-Match");
        if (value != null && !value.equalsIgnoreCase("\""+response.getContentHash()+"\"")) {
            setErrorCode(412, response, request);
            return;
        }

        // If-None-Match ; Section 14.26
        // Support this. GET or HEAD, the server SHOULD respond
        // with a 304 (Not Modified). For all other request methods,
        // the server MUST respond with 412 (Precondition Failed).
        value = request.getHeader("If-None-Match");
        if (value != null && value.equalsIgnoreCase("\""+response.getContentHash()+"\"")) {
            String method = request.getMethod();
            if ("GET".equals(method) || "HEAD".equals(method)) {
                response.setCode(304);
            } else {
                setErrorCode(412, response, request);
                return;
            }
        }

        // Range ; Section 14.35
        // Don't support this and return a response with a status
        // of 416 (Requested range not satisfiable).
        if (request.existsHeader("Range")) {
            setErrorCode(416, response, request);
            return;
        }

        // User-Agent ; Section 14.43
        // Keep track of this but don't do anything about it.

        // *** Response Headers (only support those listed below)
        //
        // ETag ; Section 14.19
        // Attach this to outgoing responses
        value = response.getContentHash();
        if (value != null) {
            response.putHeader("ETag", "\""+value+"\"");
        }

        // Server ; Section 14.38
        // Sure why not? Server: TinyWebServer/1.0
        response.putHeader("Server", "TinyWebServer/1.0");

        // Vary ; Section 14.44
        // Don't send this. Just let the normal ETag behaviour
        // decide if a cached item is available.

        // *** Entity Headers (only support those listed below)
        //
        // Content-Encoding ; Section 14.11
        // Don't support zip or other encodings. If the content-coding
        // of an entity in a request message is not acceptable then respond
        // with a 415 (Unsupported Media Type).
        if (request.existsHeader("Content-Encoding")) {
            setErrorCode(415, response, request);
            return;
        }

        // Content-Length ; Section 14.13
        // The Content-Length entity-header field indicates the size of the
        // entity-body, in decimal number of OCTETs, sent to the recipient or,
        // in the case of the HEAD method, the size of the entity-body that
        // would have been sent had the request been a GET.
        if (response.getContentLength() >= 0) {
            response.putHeader("Content-Length", Long.toString(response.getContentLength()));
        }

        // Content-Type ; Section 14.17
        // The Content-Type entity-header field indicates the media type of the
        // entity-body sent to the recipient or, in the case of the HEAD method,
        // the media type that would have been sent had the request been a GET.
        if (response.getContentType() != null) {
            response.putHeader("Content-Type", response.getContentType());
        }
    }

    private static void setErrorCode(int code, HttpResponse response, HttpRequest request) {
        response.clear();

        response.setCode(code);
        response.putHeader("Date", dateFormat.format(Calendar.getInstance().getTime()));
        response.putHeader("Server", "TinyWebServer/1.0");

        // Connection ; Section 14.10
        // Must support this and handle and send a "close" if the client requests a close.
        // Must send a close if the request Connection header is missing or is not Keep-Alive.
        // Should send a Keep-Alive if the request contained a keep alive.
        String value = request.getHeader("Connection");
        if ("close".equalsIgnoreCase(request.getHeader("Connection"))) {
            response.putHeader("Connection", "close");
        }
        if (value == null) {
            response.putHeader("Connection", "close");
        }
        if ("Keep-Alive".equalsIgnoreCase(value)) {
            response.putHeader("Connection", "Keep-Alive");
        }
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
