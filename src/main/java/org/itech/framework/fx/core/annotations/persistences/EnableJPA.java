package org.itech.framework.fx.core.annotations.persistences;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnableJPA {
    boolean value() default true;
    String entitiesPackage() default "";
}
