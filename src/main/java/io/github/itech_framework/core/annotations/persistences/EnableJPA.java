package io.github.itech_framework.core.annotations.persistences;

import io.github.itech_framework.core.annotations.ComponentScan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables and configures JPA (Java Persistence API) integration
 * <p>
 * Activates JPA support and configures entity management when applied to a main configuration class.
 * Typically used in conjunction with {@link ComponentScan} for full persistence layer configuration.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see ComponentScan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnableJPA {
    /**
     * Enables or disables JPA auto-configuration
     * <p>
     * When set to {@code true} (default), configures:
     * <ul>
     *   <li>Entity manager factory</li>
     *   <li>Transaction management</li>
     *   <li>JPA repository support</li>
     * </ul>
     * </p>
     * @return {@code true} to enable JPA support, {@code false} to disable
     */
    boolean value() default true;

    /**
     * Base package to scan for JPA entities
     * <p>
     * Specifies the package containing @Entity classes.
     * If not specified, defaults to the package of the class annotated with {@code @EnableJPA}.
     * </p>
     * <p>
     * Example:
     * <pre>{@code
     * @EnableJPA(entitiesPackage = "com.example.domain.entities")
     * }</pre>
     * </p>
     * @return The base package name for entity scanning
     */
    String entitiesPackage() default "";
}