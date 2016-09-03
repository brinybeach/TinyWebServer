package com.brinybeach.tinywebserver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: bryantbunderson
 * Date: 8/30/16
 * Time: 3:29 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE )
public @interface HttpController {
}
