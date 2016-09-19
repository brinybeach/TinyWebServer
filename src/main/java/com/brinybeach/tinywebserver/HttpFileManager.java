package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Return attributes of file in the file system
 * and map file extensions to mime types.
 *
 * author: bryantbunderson
 */
public class HttpFileManager {
    private static final Logger logger = LogManager.getLogger(HttpFileManager.class);

    /**
     * Some common file extensions mapped to content types
     */
    private static final Map<String, String> contentTypeMap = new HashMap<String, String>();
    {
        contentTypeMap.put(".asx", "application/x-mplayer2");
        contentTypeMap.put(".au", "audio/basic");
        contentTypeMap.put(".avi", "application/x-troff-msvideo");
        contentTypeMap.put(".bin", "application/mac-binary");
        contentTypeMap.put(".bm", "image/bmp");
        contentTypeMap.put(".bmp", "image/bmp");
        contentTypeMap.put(".bz", "application/x-bzip");
        contentTypeMap.put(".bz2", "application/x-bzip2");
        contentTypeMap.put(".css", "text/css");
        contentTypeMap.put(".doc", "application/msword");
        contentTypeMap.put(".dv", "video/x-dv");
        contentTypeMap.put(".dvi", "application/x-dvi");
        contentTypeMap.put(".fli", "video/fli");
        contentTypeMap.put(".gz", "application/x-gzip");
        contentTypeMap.put(".gzip", "application/x-gzip");
        contentTypeMap.put(".htm", "text/html");
        contentTypeMap.put(".html", "text/html");
        contentTypeMap.put(".ico", "image/x-icon");
        contentTypeMap.put(".jfif", "image/jpeg");
        contentTypeMap.put(".jpeg", "image/jpeg");
        contentTypeMap.put(".jpg", "image/jpeg");
        contentTypeMap.put(".js", "application/javascript");
        contentTypeMap.put(".m3u", "audio/x-mpequrl");
        contentTypeMap.put(".mid", "application/x-midi");
        contentTypeMap.put(".midi", "application/x-midi");
        contentTypeMap.put(".mjpg", "video/x-motion-jpeg");
        contentTypeMap.put(".mov", "video/quicktime");
        contentTypeMap.put(".mp2", "audio/mpeg");
        contentTypeMap.put(".mp3", "audio/mpeg3");
        contentTypeMap.put(".mpeg", "video/mpeg");
        contentTypeMap.put(".mpg", "audio/mpeg");
        contentTypeMap.put(".pdf", "application/pdf");
        contentTypeMap.put(".png", "image/png");
        contentTypeMap.put(".ppm", "image/x-portable-pixmap");
        contentTypeMap.put(".pps", "application/mspowerpoint");
        contentTypeMap.put(".ps", "application/postscript");
        contentTypeMap.put(".qif", "image/x-quicktime");
        contentTypeMap.put(".qt", "video/quicktime");
        contentTypeMap.put(".ra", "audio/x-pn-realaudio");
        contentTypeMap.put(".rtf", "application/rtf");
        contentTypeMap.put(".shtml", "text/html");
        contentTypeMap.put(".sprite", "application/x-sprite");
        contentTypeMap.put(".text", "application/plain");
        contentTypeMap.put(".tgz", "application/gnutar");
        contentTypeMap.put(".tif", "image/tiff");
        contentTypeMap.put(".tiff", "image/tiff");
        contentTypeMap.put(".txt", "text/plain");
        contentTypeMap.put(".voc", "audio/voc");
        contentTypeMap.put(".wav", "audio/wav");
        contentTypeMap.put(".wp", "application/wordperfect");
        contentTypeMap.put(".xbm", "image/x-xbitmap");
        contentTypeMap.put(".xls", "application/excel");
        contentTypeMap.put(".xm", "audio/xm");
        contentTypeMap.put(".xml", "application/xml");
        contentTypeMap.put(".x-png", "image/png");
        contentTypeMap.put(".zip", "application/x-compressed");
    }

    private static final HttpServerConfig config = HttpServerConfig.getInstance();
    private static final HttpFileManager instance = new HttpFileManager();

    private String rootDir;


    public static HttpFileManager getInstance() {
        return instance;
    }

    private HttpFileManager() {
        rootDir = config.getDirectory();
        logger.debug("rootDir is "+rootDir);
    }

    public boolean exists(String uri) {
        File file = new File(rootDir + uri);
        return file.exists();
    }

    public boolean isDirectory(String uri) {
        File file = new File(rootDir + uri);
        return (file.exists() && file.isDirectory());
    }

    public long getLength(String uri) {
        File file = new File(rootDir + uri);
        return file.length();
    }

    public String getContentType(String uri) {
        String contentType = "text/plain";

        try {
            contentType = contentTypeMap.get(uri.substring(uri.indexOf('.')));
            if (contentType == null) throw new RuntimeException();
        } catch (Exception ignore) {}

        return contentType;
    }

    public String getHash(String uri) {
        File file = new File(rootDir + uri);
        return Integer.toHexString(String.format("%s-%d-%d", uri, file.length(), file.lastModified()).hashCode());
    }

    public InputStream getInputStream(String uri) throws FileNotFoundException {
        File file = new File(rootDir + uri);
        return new FileInputStream(file);
    }

    public String getAbsolutePath(String uri) {
        File file = new File(rootDir + uri);
        return file.getAbsolutePath();
    }

}
