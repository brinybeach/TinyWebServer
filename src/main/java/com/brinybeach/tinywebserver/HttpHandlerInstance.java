package com.brinybeach.tinywebserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: bryantbunderson
 * Date: 9/1/16
 * Time: 3:27 AM
 */
class HttpHandlerInstance {
    private static final Logger logger = LogManager.getLogger(HttpHandlerInstance.class);

    private Object instance;
    private Method method;

    public HttpHandlerInstance(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    public HttpResponse invokeHandler(HttpRequest request) {
        try {
            return (HttpResponse) method.invoke(instance, request);
        } catch (IllegalAccessException e) {
            logger.error(e);
        } catch (InvocationTargetException e) {
            logger.error(e);
        }

        return null;
    }
}
