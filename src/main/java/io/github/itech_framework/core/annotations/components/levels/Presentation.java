package io.github.itech_framework.core.annotations.components.levels;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a presentation layer component
 * <p>
 * Identifies classes that handle user interface interactions, request processing,
 * and response formatting. Specialization of {@link Component} typically used for:
 * </p>
 *  <ul>
 *      <li>MVC controllers</li>
 *      <li>UI event handlers</li>
 *  </ul>
 *
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Presentation {
}