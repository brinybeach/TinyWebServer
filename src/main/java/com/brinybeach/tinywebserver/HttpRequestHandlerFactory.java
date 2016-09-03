package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 2:22 AM
 */
public class HttpRequestHandlerFactory {
    private static final Logger logger = LogManager.getLogger(HttpRequestHandlerFactory.class);

    private static HttpRequestHandlerFactory instance;
    private Map<HttpRequestHandler, HttpHandlerInstance> handlerInstances = new HashMap<HttpRequestHandler, HttpHandlerInstance>();

    public static HttpRequestHandlerFactory getInstance() {
        if (instance == null) {
            instance = new HttpRequestHandlerFactory();
        }
        return instance;
    }

    public HttpRequestHandlerFactory() {
        String packageName = Application.class.getPackage().getName();

        try {
            Class[] classes = getClasses(packageName);
            for (Class clazz : classes) {
                if (clazz.isAnnotationPresent(HttpController.class)) {
                    // System.out.println(clazz.getName());

                    for (Method method : clazz.getMethods()) {
                        if (method.isAnnotationPresent(HttpRequestHandler.class)) {
                            // System.out.println(method.getName());

                            HttpRequestHandler handler = method.getAnnotation(HttpRequestHandler.class);
                            Object handlerInstance = clazz.newInstance();

                            handlerInstances.put(handler, new HttpHandlerInstance(handlerInstance, method));
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (IllegalAccessException e) {
            logger.error(e);
        } catch (ClassNotFoundException e) {
            logger.error(e);
        } catch (InstantiationException e) {
            logger.error(e);
        }
    }

    public HttpHandlerInstance findHandlerMethod(HttpRequest request) {

        String requestMethod = request.getMethod();
        String requestUri = request.getUri();

        // Return the first handler method that matches the request URI.
        for (HttpRequestHandler handler : handlerInstances.keySet()) {
            String handlerMethod = handler.method();
            String handlerUri = handler.uri();

            if (requestUri.startsWith(handlerUri)) {
                if (handlerMethod.equals(requestMethod))
                    return handlerInstances.get(handler);

                if (handlerMethod.equals(""))
                    return handlerInstances.get(handler);
            }
        }

        return null;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     * adapted from article by Victor Tatai Nov. 30, 07 Java Zone
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) return new Class[0];

        String path = packageName.replace('.', '/');

        List<File> dirs = new ArrayList<File>();
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     * adapted from article by Victor Tatai Nov. 30, 07 Java Zone
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) return classes;

        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }

}
