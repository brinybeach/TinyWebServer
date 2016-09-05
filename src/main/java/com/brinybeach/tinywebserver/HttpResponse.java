package com.brinybeach.tinywebserver;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrap the HTTP response attributes.
 *
 * author: bryantbunderson
 */
public class HttpResponse {
    private static final String defaultVersion = "HTTP/1.1";

    private String version;
    private int code = -1;
    private String reason;
    private Map<String, String> headers = new HashMap<String, String>();

    private InputStream contentInputStream;
    private long contentLength = -1;
    private String contentType;
    private String contentHash;


    // Force a code to be used when creating a response
    private HttpResponse() {
    }

    /**
     * Create an empty HttpResponse from the status code
     * @param code the HTTP status code
     */
    public HttpResponse(int code) {
        setVersion(defaultVersion);
        setCode(code);
    }

    /**
     * Create a HttpResponse populated from a file
     *
     * @param code the HTTP status code
     * @param contentUri the path to the file on the local file system
     * @throws FileNotFoundException if the file isn't found
     */
    public HttpResponse(int code, String contentUri) throws FileNotFoundException {
        setVersion(defaultVersion);
        setCode(code);
        setContentWithUri(contentUri);
    }

    /**
     * Create a HttpResponse populated from the InputStream
     *
     * @param code the status Code
     * @param inputStream the InputStream with the characters
     * @param length the expected length of the content
     * @param contentType the content mimi-type
     */
    public HttpResponse(int code, InputStream inputStream, long length, String contentType) {
        setVersion(defaultVersion);
        setCode(code);
        setContentWithInputStream(inputStream, length, contentType);
    }

    /**
     * @return the HTTP version HTTP/1.0 or HTTP/1.1
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the HTTP version HTTP/1.0 or HTTP/1.1
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the HTTP status code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code set the HTTP status code
     */
    public void setCode(int code) {
        this.code = code;
        this.reason = reasonMap.get(code);
    }

    /**
     * The reason phrase is set when the setCode() method is called.
     *
     * @return the HTTP reason phrase
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param name the field name of the header
     * @return return true if the header field exists
     */
    public boolean existsHeader(String name) {
        return headers.containsKey(name);
    }

    /**
     * @param name the field name of the header
     * @return the matching header value
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Add a header to the response
     * @param name the field name
     * @param value the field value
     */
    public void putHeader(String name, String value) {
        this.headers.put(name, value);
    }

    /**
     * @return the hash generated by the HttpFileManager
     */
    public String getContentHash() {
        return contentHash;
    }

    /**
     * @param contentHash a hash generated by the HttpFileManager
     */
    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    /**
     * @return the InputStream for reading the content body
     */
    public InputStream getContentInputStream() {
        return contentInputStream;
    }

    /**
     * @param contentInputStream the stream that contains the content body. Can by a FileInputStream or ByteArrayInputStream.
     */
    public void setContentInputStream(InputStream contentInputStream) {
        this.contentInputStream = contentInputStream;
    }

    /**
     * @return the length of the content
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * @param contentLength set the content length
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     *
     * @return the mime-type set using the HttpFileManager
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the mime-type from the HttpFileManager
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the content based on a local filename.
     * @param uri the filename of the local file
     * @throws FileNotFoundException if the file isn't found
     */
    public void setContentWithUri(String uri) throws FileNotFoundException {
        HttpFileManager fileManager = HttpFileManager.getInstance();

        setContentInputStream(fileManager.getInputStream(uri));
        setContentLength(fileManager.getLength(uri));
        setContentType(fileManager.getContentType(uri));
        setContentHash(fileManager.getHash(uri));
    }

    /**
     * Set the content based on the input stream.
     *
     * @param inputStream the InputStream containing the content characters
     * @param length the expected length of the content
     * @param contentType the mime-type using HttpFileManager
     */
    public void setContentWithInputStream(InputStream inputStream, long length, String contentType) {
        setContentInputStream(inputStream);
        setContentLength(length);
        setContentType(contentType);
        setContentHash(null);
    }

    /**
     * Clear all attributes in this HttpResponse object
     */
    public void clear() {
        setVersion(defaultVersion);

        code = -1;
        reason = null;
        headers.clear();

        contentInputStream = null;
        contentLength = -1;
        contentType = null;
        contentHash = null;
    }

    /**
     * Write the attributes to the output stream formatted as per the HTTP RFC spec.
     * @param outputStream the OutputStream to write to
     * @throws IOException if it can't
     */
    public void write(OutputStream outputStream) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(outputStream);

        out.write(version.getBytes());
        out.write(' ');
        out.write(Integer.toString(code).getBytes());
        out.write(' ');
        out.write(reason.getBytes());
        out.write("\r\n".getBytes());

        for (String name : headers.keySet()) {
            String value = headers.get(name);

            out.write(name.getBytes());
            out.write(": ".getBytes());
            out.write(value.getBytes());
            out.write("\r\n".getBytes());
        }

        out.write("\r\n".getBytes());

        if (contentInputStream != null && contentLength > 0) {
            BufferedInputStream inputStream = new BufferedInputStream(contentInputStream);

            byte buffer[] = new byte[1024];

            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            if (totalBytes != contentLength) {
                // TODO: How do I handle this?
            }

            inputStream.close();
        }

        out.flush();
    }

    private static final Map<Integer, String> reasonMap = new HashMap<Integer, String>();
    {
        reasonMap.put(100, "Continue");
        reasonMap.put(101, "Switching Protocols");
        reasonMap.put(200, "OK");
        reasonMap.put(201, "Created");
        reasonMap.put(202, "Accepted");
        reasonMap.put(203, "Non-Authoritative Information");
        reasonMap.put(204, "No Content");
        reasonMap.put(205, "Reset Content");
        reasonMap.put(206, "Partial Content");
        reasonMap.put(300, "Multiple Choices");
        reasonMap.put(301, "Moved Permanently");
        reasonMap.put(302, "Found");
        reasonMap.put(303, "See Other");
        reasonMap.put(304, "Not Modified");
        reasonMap.put(305, "Use Proxy");
        reasonMap.put(307, "Temporary Redirect");
        reasonMap.put(400, "Bad Request");
        reasonMap.put(401, "Unauthorized");
        reasonMap.put(402, "Payment Required");
        reasonMap.put(403, "Forbidden");
        reasonMap.put(404, "Not Found");
        reasonMap.put(405, "Method Not Allowed");
        reasonMap.put(406, "Not Acceptable");
        reasonMap.put(407, "Proxy Authentication Required");
        reasonMap.put(408, "Request Timeout");
        reasonMap.put(409, "Conflict");
        reasonMap.put(410, "Gone");
        reasonMap.put(411, "Length Required");
        reasonMap.put(412, "Precondition Failed");
        reasonMap.put(413, "Payload Too Large");
        reasonMap.put(414, "URI Too Long");
        reasonMap.put(415, "Unsupported Media Type");
        reasonMap.put(416, "Range Not Satisfiable");
        reasonMap.put(417, "Expectation Failed");
        reasonMap.put(426, "Upgrade Required");
        reasonMap.put(500, "Internal Server Error");
        reasonMap.put(501, "Not Implemented");
        reasonMap.put(502, "Bad Gateway");
        reasonMap.put(503, "Service Unavailable");
        reasonMap.put(504, "Gateway Timeout");
        reasonMap.put(505, "HTTP Version Not Supported");
    }
}
