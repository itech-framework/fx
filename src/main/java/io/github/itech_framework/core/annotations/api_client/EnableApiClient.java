package io.github.itech_framework.core.annotations.api_client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables and configures API client support in the ITech Framework
 * <p>
 * When applied to a main application class, this annotation activates the API client infrastructure
 * and configures the scanning for API client interfaces. Typically used in conjunction with
 * {@link io.github.itech_framework.core.annotations.ComponentScan} for comprehensive component management.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableApiClient {
    /**
     * Enables or disables the API client auto-configuration
     * <p>
     * When set to {@code true} (default), the framework will automatically:
     * <ul>
     *   <li>Scan for interfaces annotated with @ApiClient</li>
     *   <li>Generate implementation classes at runtime</li>
     *   <li>Register API clients in the application context</li>
     * </ul>
     * </p>
     * @return {@code true} to enable API client support, {@code false} to disable
     */
    boolean value() default true;

}