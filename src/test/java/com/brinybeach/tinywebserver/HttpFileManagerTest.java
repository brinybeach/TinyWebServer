package com.brinybeach.tinywebserver;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 12:51 PM
 */
public class HttpFileManagerTest extends TestCase {
    private static final String uri = "/test/test.html";
    private static final HttpFileManager fileManager = HttpFileManager.getInstance();

    public void testFileManagerExistsUri() {
        assertTrue(fileManager.exists(uri));
    }

    public void testFileManagerGetLength() {
        long length = fileManager.getLength(uri);
        assertEquals(183, length);
    }

    public void testFileManagerGetContentType() {
        String contentType = fileManager.getContentType(uri);
        assertEquals("text/html", contentType);
    }

    public void testFileManagerInputStream() throws IOException {
        int length = Math.min((int) fileManager.getLength(uri), 4096);
        assertTrue(length > 0);

        InputStream inputStream = fileManager.getInputStream(uri);
        assertNotNull(inputStream);

        byte buffer[] = new byte[1024];

        int bytesRead;
        int totalBytes = 0;

        while ((bytesRead = inputStream.read(buffer)) > 0) {
            totalBytes += bytesRead;
        }

        assertEquals(length, totalBytes);
    }

    public void testFileManagerGetHash() {
        String hash = fileManager.getHash(uri);
        assertEquals("a87ac353", hash);
    }
}
