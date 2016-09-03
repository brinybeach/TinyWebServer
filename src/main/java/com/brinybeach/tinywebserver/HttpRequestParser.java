package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: bryantbunderson
 * Date: 8/31/16
 *
 * A simple resursive descent parser that
 * follows RFC-2616 to parse HTTP requests.
 *
 */
public class HttpRequestParser {
    private static final Logger logger = LogManager.getLogger(HttpRequestParser.class);

    private String method;
    private String uri;
    private String query;
    private String version;
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;

    private BufferedInputStream inputStream;
    private StringBuffer buffer = new StringBuffer();
    private int offset;

    /**
     * 5 Request
     *
     * Request       = Request-Line              ; Section 5.1
     *                 *(( general-header        ; Section 4.5
     *                  | request-header         ; Section 5.3
     *                  | entity-header ) CRLF)  ; Section 7.1
     *                  CRLF
     *                  [ message-body ]          ; Section 4.3
     *
     * @param inputStream
     * @return
     */
    public HttpRequest parse(InputStream inputStream) throws IOException {
        this.inputStream = new BufferedInputStream(inputStream);

        try {
            parseRequestLine();
            parseHeaders();

            int offset = this.offset;
            char c;

            c = getChar(offset);
            if (c != '\r') throw new ParseException("Missing CRLF after last header", offset);
            offset++;
            this.offset = offset;

            c = getChar(offset);
            if (c != '\n') throw new ParseException("Missing CRLF after last header", offset);
            offset++;
            this.offset = offset;

            parseBody();

            // Return a valid request meaning that there weren't any parse errors.
            return new HttpRequest(method, uri, query, version, headers, body, true);
        } catch (ParseException e) {
            logger.warn(e);

            // Go ahead and return an invalid request and let
            // the downstream consumers decide how to handle it.
            return new HttpRequest(method, uri, query, version, headers, body, false);
        }
    }

    /**
     * 5.1 Request-Line
     *
     * Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
     */
    private void parseRequestLine() throws ParseException, IOException {
        int offset = this.offset;
        char c;

        parseMethod();
        offset = this.offset;

        c = getChar(offset);
        if (c != ' ') throw new ParseException("Bad character in METHOD", offset);
        offset++; this.offset = offset;

        parseRequestURI();
        offset = this.offset;

        c = getChar(offset);
        if (c != ' ') throw new ParseException("Bad character in URI", offset);
        offset++; this.offset = offset;

        parseHttpVersion();
        offset = this.offset;

        c = getChar(offset);
        if (c != '\r') throw new ParseException("Bad character in HTTP version", offset);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != '\n') throw new ParseException("Bad character in HTTP version", offset);
        offset++; this.offset = offset;
    }

    /**
     * 5.1.1 Method
     *
     * Method         = "OPTIONS"                ; Section 9.2
     *                | "GET"                    ; Section 9.3
     *                | "HEAD"                   ; Section 9.4
     *                | "POST"                   ; Section 9.5
     *                | "PUT"                    ; Section 9.6
     *                | "DELETE"                 ; Section 9.7
     *                | "TRACE"                  ; Section 9.8
     *                | "CONNECT"                ; Section 9.9
     *                | extension-method
     * extension-method = token
     */
    private void parseMethod() throws ParseException, IOException {
        int offset;

        offset = this.offset;
        if ((getChar(offset++) == 'G') && (getChar(offset++) == 'E') && (getChar(offset++) == 'T')) {
            this.method = "GET";
            this.offset = offset;
            return;
        }

        offset = this.offset;
        if ((getChar(offset++) == 'H') && (getChar(offset++) == 'E') && (getChar(offset++) == 'A') && (getChar(offset++) == 'D')) {
            this.method = "HEAD";
            this.offset = offset;
            return;
        }

        offset = this.offset;
        if ((getChar(offset++) == 'P') && (getChar(offset++) == 'O') && (getChar(offset++) == 'S') && (getChar(offset++) == 'T')) {
            this.method = "POST";
            this.offset = offset;
            return;
        }

        offset = this.offset;
        if ((getChar(offset++) == 'P') && (getChar(offset++) == 'U') && (getChar(offset++) == 'T')) {
            this.method = "PUT";
            this.offset = offset;
            return;
        }

        offset = this.offset;
        if ((getChar(offset++) == 'D') && (getChar(offset++) == 'E') && (getChar(offset++) == 'L') &&
            (getChar(offset++) == 'E') && (getChar(offset++) == 'T') && (getChar(offset++) == 'E')) {

            this.method = "DELETE";
            this.offset = offset;
            return;
        }

        throw new ParseException("Bad request method", offset);
    }

    /**
     * 5.1.2 Request-URI
     * Request-URI    = "*" | absoluteURI | abs_path | authority
     *
     * example: GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1
     *
     * Only support "abs_path" from RFC-2396 because none of the Web clients
     * that I've tried actually contain any other kind of path in the request.
     * That should be good enough for the purposes of the Adobe Web Tech test.
     *
     * In other words, only paths that start with a "/" are supported.
     *
     * 3. URI Syntactic Components (RFC-2396)
     *
     * <scheme>://<authority><path>?<query>
     *
     * absoluteURI   = scheme ":" ( hier_part | opaque_part )
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     * net_path      = "//" authority [ abs_path ]
     * abs_path      = "/"  path_segments
     *
     * path_segments = segment *( "/" segment )
     */
    private void parseRequestURI() throws ParseException, IOException {
        int offset = this.offset;

        char c = getChar(offset);
        if (c != '/') throw new ParseException("Only abs_path supported", offset);

        // First use of uri so create the
        // String instead of appending to it.
        uri = Character.toString(c);
        offset++; this.offset = offset;

        parseSegment();

        while (this.offset > offset) {
            offset = this.offset;
            c = getChar(offset);

            if (c == '/') {
                uri += Character.toString(c);
                offset++; this.offset = offset;

                parseSegment();
            }
        }

        parseQuery();
    }

    /**
     * 3.3. Path Component (RFC-2396)
     *
     * segment       = *pchar *( ";" param )
     * param         = *pchar
     */
    private void parseSegment() throws ParseException, IOException {
        int offset;
        char c;

        offset = this.offset;
        c = getChar(offset);
        while (isPchar(c)) {
            parsePchar();

            offset = this.offset;
            c = getChar(offset);
        }

        offset = this.offset;
        c = getChar(offset);
        while (c == ';') {
            uri += Character.toString(c);
            offset++; this.offset = offset;

            parseParam();

            offset = this.offset;
            c = getChar(offset);
        }

        this.offset = offset;
    }

    /**
     * 3. URI Syntactic Components (RFC-2396)
     *
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     *
     * 3.4. Query Component (RFC-2396)
     *
     * query         = *uric
     *
     * Within a query component, the characters ";", "/", "?", ":", "@",
     * "&", "=", "+", ",", and "$" are reserved.
     */
    private void parseQuery() throws ParseException, IOException {
        int offset = this.offset;

        char c = getChar(offset);
        if (c != '?') return;

        query = Character.toString(c);
        offset++; this.offset = offset;

        // Just parse to the space
        // before the HTTP version
        c = getChar(offset);
        while (c != ' ') {
            query += Character.toString(c);
            offset++; this.offset = offset;

            c = getChar(offset);
        }
    }

    /**
     * 3.3. Path Component (RFC-2396)
     *
     * pchar         = unreserved | escaped |
     *                 ":" | "@" | "&" | "=" | "+" | "$" | ","
     *
     * unreserved    = alphanum | mark
     * mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     */
    private void parsePchar() throws ParseException, IOException {
        int offset = this.offset;

        char c = getChar(offset);
        while (isPchar(c)) {
            if (isEscaped(c)) {
                parseEscaped();
                offset = this.offset;
            } else {
                uri += Character.toString(c);
                offset++; this.offset = offset;
            }

            c = getChar(offset);
        }

        this.offset = offset;
    }

    /**
     * 3.3. Path Component (RFC-2396)
     *
     * param         = *pchar
     */
    private void parseParam() throws ParseException, IOException {
        int offset = this.offset;

        char c = getChar(offset);
        while (isPchar(c)) {
            uri += Character.toString(c);
            c = getChar(offset++);
        }

        this.offset = offset;
    }

    /**
     * 2.4.1. Escaped Encoding (RFC-2396)
     *
     * An escaped octet is encoded as a character triplet, consisting of the
     * percent character "%" followed by the two hexadecimal digits
     * representing the octet code. For example, "%20" is the escaped
     * encoding for the US-ASCII space character.
     *
     * escaped       = "%" hex hex
     * hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     */
    private void parseEscaped() throws ParseException, IOException {
        int offset = this.offset;

        char c = getChar(offset);
        if (c != '%') throw new ParseException("Invalid escaped hex number", offset);

        uri += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (!isHex(c))  throw new ParseException("Invalid escaped hex number", offset);

        uri += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (!isHex(c))  throw new ParseException("Invalid escaped hex number", offset);

        uri += Character.toString(c);
        offset++; this.offset = offset;
    }

    /**
     * 3.1 HTTP Version
     * HTTP-Version   = "HTTP" "/" 1*DIGIT "." 1*DIGIT
     */
    private void parseHttpVersion() throws ParseException, IOException {
        int offset = this.offset;
        char c;

        c = getChar(offset);
        if (c != 'H') throw new ParseException("Invalid HTTP version", offset);
        version = Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != 'T') throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != 'T') throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != 'P') throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != '/') throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (!isDigit(c)) throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != '.') throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (!isDigit(c)) throw new ParseException("Invalid HTTP version", offset);
        version += Character.toString(c);
        offset++; this.offset = offset;
    }

    /**
     * 5.3 Request Header Fields
     *
     * request-header = Accept                   ; Section 14.1
     *                | Accept-Charset           ; Section 14.2
     *                | Accept-Encoding          ; Section 14.3
     *                | Accept-Language          ; Section 14.4
     *                | Authorization            ; Section 14.8
     *                | Expect                   ; Section 14.20
     *                | From                     ; Section 14.22
     *                | Host                     ; Section 14.23
     *                | If-Match                 ; Section 14.24
     *                | If-Modified-Since        ; Section 14.25
     *                | If-None-Match            ; Section 14.26
     *                | If-Range                 ; Section 14.27
     *                | If-Unmodified-Since      ; Section 14.28
     *                | Max-Forwards             ; Section 14.31
     *                | Proxy-Authorization      ; Section 14.34
     *                | Range                    ; Section 14.35
     *                | Referer                  ; Section 14.36
     *                | TE                       ; Section 14.39
     *                | User-Agent               ; Section 14.43
     */
    private void parseHeaders() throws ParseException, IOException {
        int offset;

        do {
            offset = this.offset;
            parseHeader();
        } while (this.offset > offset);
    }

    /**
     * 4.2 Message Headers
     *
     * message-header = field-name ":" [ field-value ] CRLF
     *                  field-name     = token
     *                  field-value    = *( field-content | LWS )
     *                  field-content  = <the OCTETs making up the field-value
     *                                   and consisting of either *TEXT or combinations
     *                                   of token, separators, and quoted-string>
     */
    private void parseHeader() throws ParseException, IOException {
        int offset = this.offset;
        char c;

        StringBuffer fieldName = new StringBuffer();
        StringBuffer fieldValue = new StringBuffer();

        c = getChar(offset);
        if (!isToken(c)) return;

        while (isToken(c)) {
            fieldName.append(c);
            offset++; this.offset = offset;

            c = getChar(offset);
        }

        if (c != ':') throw new ParseException("Bad character in header field-name", offset);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != ' ') throw new ParseException("Bad character in header field-value", offset);
        offset++; this.offset = offset;

        c = getChar(offset);
        while (c != '\r') {
            fieldValue.append(c);
            offset++; this.offset = offset;

            c = getChar(offset);
        }

        if (c != '\r') throw new ParseException("Bad character after header", offset);
        offset++; this.offset = offset;

        c = getChar(offset);
        if (c != '\n') throw new ParseException("Bad character after header", offset);
        offset++; this.offset = offset;

        headers.put(fieldName.toString(), fieldValue.toString());
    }

    /**
     * 4.3 Message Body
     *
     * message-body = entity-body
     *              | <entity-body encoded as per Transfer-Encoding>
     *
     * 7.2 Entity Body
     *
     * entity-body   = *OCTET
     */
    private void parseBody() throws ParseException {
        int offset = this.offset;

        if (headers.containsKey("Content-Length")) {
            int length = Integer.parseInt(headers.get("Content-Length"));
            byte buffer[] = new byte[Math.min(1024, length)];

            int totalBytes = 0;
            while (totalBytes < length) {
                int bytesRead;

                try {
                    bytesRead = inputStream.read(buffer);
                    totalBytes += bytesRead;
                } catch (Exception e) {
                    throw new ParseException("Exception reading message body", e, offset);
                }
                if (bytesRead == -1) {
                    throw new ParseException("End of stream reading message body", offset);
                }

                body += new String(buffer, 0, bytesRead);
                offset += bytesRead;
            }
        }

        this.offset = offset;
    }

    /**
     * Is the char a digit. Lifted from JDK source.
     * @param c
     * @return true or false
     */
    private boolean isDigit(char c) {
        return Character.getType(c) == Character.DECIMAL_DIGIT_NUMBER;
    }

    /**
     * Is the char alphabetic. Lifted from JDK source.
     * @param c
     * @return true or false
     */
    private boolean isAlpha(char c) {
        return ((((((1 << Character.UPPERCASE_LETTER) | (1 << Character.LOWERCASE_LETTER))) >> Character.getType(c)) & 1) != 0);
    }

    /**
     * 3.3. Path Component (RFC-2396)
     *
     * @param c
     * @return true or false
     */
    private boolean isPchar(char c) {
        return (isUnreserved(c) || isEscaped(c) || c == ':' || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',');
    }

    /**
     * 2.3. Unreserved Characters (RFC-2396)
     *
     * @param c
     * @return true or false
     */
    private boolean isUnreserved(char c) {
        return (isAlpha(c) || isDigit(c) || isMark(c));
    }

    /**
     * 2.3. Unreserved Characters (RFC-2396)
     *
     * @param c
     * @return true or false
     */
    private boolean isMark(char c) {
        return (c == '-' || c == '_' || c == '.' || c == '!' || c == '~' || c == '*' | c == '\'' || c == '(' || c == ')');
    }

    /**
     * 2.4.1. Escaped Encoding (RFC-2396)
     *
     * escaped     = "%" hex hex
     *
     * @param c
     * @return true or false
     */
    private boolean isEscaped(char c) {
        return (c == '%');
    }

    /**
     * 2.4.1. Escaped Encoding (RFC-2396)
     *
     * hex         = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                       "a" | "b" | "c" | "d" | "e" | "f"
     * @param c
     * @return true or false
     */
    private boolean isHex(char c) {
        return (isDigit(c) || c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F' ||
                              c == 'a' || c == 'b' || c == 'c' || c == 'd' || c ==  'e' || c ==  'f');
    }

    /**
     * 2.2 Basic Rules
     *
     * token          = 1*<any CHAR except CTLs or separators>
     *
     * @param c
     * @return true or false
     */
    private boolean isToken(char c) {
        return (!isCtl(c) && !isSeparator(c));
    }

    /**
     * 2.2 Basic Rules
     *
     * separators     = "(" | ")" | "<" | ">" | "@"
     *                | "," | ";" | ":" | "\" | <">
     *                | "/" | "[" | "]" | "?" | "="
     *                | "{" | "}" | SP | HT
     * 
     * @param c
     * @return true or false
     */
    private boolean isSeparator(char c) {
        return (c == '(' || c == ')' || c == '<' || c == '>'  || c == '@'
             || c == ',' || c == ';' || c == ':' || c == '\\' || c == '"'
             || c == '/' || c == '[' || c == ']' || c == '?'  || c == '='
             || c == '{' || c == '}' || c == ' ' || c == '\t');
    }

    /**
     * 2.2 Basic Rules
     *
     * CTL            = <any US-ASCII control character
     *                  (octets 0 - 31) and DEL (127)>
     * @param c
     * @return true or false
     */
    private boolean isCtl(char c) {
        return Character.isISOControl(c);
    }


    /**
     * Get a character at "offset" from the inputStream or the
     * internal StringBuffer. The StringBuffer is there so that
     * the parser can backup if it needs to.
     *
     * @param offset of the char to get
     * @return the char from the stream or buffer
     * @throws ParseException
     */
    private char getChar(int offset) throws IOException {
        while (offset >= buffer.length()) {
            int c;

            try {
                c = inputStream.read();
            } catch (IOException e) {
                throw new IOException("Exception reading next character", e);
            }
            if (c == -1) {
                throw new IOException("End of stream");
            }

            buffer.append((char) c);
        }

        return buffer.charAt(offset);
    }

    class ParseException extends Exception {
        private int offset;

        public ParseException(String message, int offset) {
            super(message);
            this.offset = offset;
        }

        public ParseException(String message, Throwable cause, int offset) {
            super(message, cause);
            this.offset = offset;
        }
    }
}
