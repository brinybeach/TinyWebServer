package com.brinybeach.tinywebserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation is used to mark classes that contain @HttpRequestMethod methods.
 *
 * author: bryantbunderson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE )
public @interface HttpController {
}
