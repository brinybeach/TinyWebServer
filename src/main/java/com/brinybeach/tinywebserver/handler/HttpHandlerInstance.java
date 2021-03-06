package com.brinybeach.tinywebserver.handler;

import com.brinybeach.tinywebserver.HttpRequest;
import com.brinybeach.tinywebserver.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrap an instance of the object and method that can be called
 * for dynamic content creation. This is returned from the
 * HttpRequestHandlerFactory. HttpHandlerInstance is inmutable.
 *
 * author: bryantbunderson
 */
public class HttpHandlerInstance {
    private static final Logger logger = LogManager.getLogger(HttpHandlerInstance.class);

    private Object instance;
    private Method method;

    /**
     * Create a new HttpHandlerInstance from the Object
     * and Method found by the HttpRequestHandlerFactory.
     *
     * @param instance the instance of the object that contains the handler method
     * @param method the method to call
     */
    public HttpHandlerInstance(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public Object getObject() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * Invoke the method on the instance of the object and hand it the request to handle.
     *
     * @param request the request to pass to the handler
     * @return an appropriate HttpResponse
     */
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
