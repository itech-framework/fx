package io.github.itech_framework.core.annotations.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the framework to ignore interface implementations during component processing
 * <p>
 * When applied to a class, indicates that any interfaces implemented by the annotated class
 * should not be processed or registered as components. This is particularly useful for
 * excluding standard Java interfaces (like {@link java.io.Serializable}) that inherit from
 * {@link Object} from component scanning.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IgnoreInterfaces {
}