package com.brinybeach.tinywebserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark the method used to
 * generation dynamic content. The method MUST take one
 * HttpRequest object and return a HttpResponse object.
 *
 * author: bryantbunderson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.METHOD )
public @interface HttpRequestHandler {
    String method() default "";
    String uri() default "";
}
