package org.itech.framework.fx.core.annotations.storage;

import org.itech.framework.fx.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DataStorage {
    String key() default "";
    String defaultValue() default "";
}
