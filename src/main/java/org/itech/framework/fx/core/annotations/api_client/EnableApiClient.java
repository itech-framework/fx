package org.itech.framework.fx.core.annotations.api_client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableApiClient {
    boolean value() default true;
    String[] basePackages() default {};
}
