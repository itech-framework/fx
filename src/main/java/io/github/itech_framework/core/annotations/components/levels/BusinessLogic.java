package io.github.itech_framework.core.annotations.components.levels;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a business logic service component
 * <p>
 * Indicates that the annotated class contains core business rules and workflow
 * implementations. Specialization of {@link Component} that helps distinguish
 * business logic layers from other component types in the application architecture.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface BusinessLogic {
}