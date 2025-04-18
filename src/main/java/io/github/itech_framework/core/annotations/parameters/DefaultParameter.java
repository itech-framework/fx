package io.github.itech_framework.core.annotations.parameters;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a default value for a method parameter
 * <p>
 * Provides fallback values when no argument is supplied for the annotated parameter.
 * The framework will automatically convert the string value to the parameter's declared type.
 * </p>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Configuration parameters with default settings</li>
 *   <li>Optional dependencies with fallback implementations</li>
 *   <li>Method arguments requiring default values</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @OnInit
 * public void configure(
 *     @DefaultParameter("8080") int port,
 *     @DefaultParameter("development") String environment
 * ) {
 *     // Method implementation
 * }
 * }</pre>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DefaultParameter {
    /**
     * The default value to use when no argument is provided
     * <p>
     * Value will be converted to the parameter's declared type using the framework's
     * type conversion system. Supported conversions include:
     * </p>
     * <ul>
     *   <li>Primitive types and their wrapper classes</li>
     *   <li>Strings</li>
     *   <li>Enums (using valueOf)</li>
     *   <li>Custom types with appropriate converters</li>
     * </ul>
     * 
     * @return The default value as a String representation
     */
    String value();
}