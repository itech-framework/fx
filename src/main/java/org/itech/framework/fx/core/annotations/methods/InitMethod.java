package org.itech.framework.fx.core.annotations.methods;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressWarnings({"unused", "RedundantSuppression"})
public @interface InitMethod {
    int order() default 0;
}
