package io.github.itech_framework.core.annotations.constructor;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates the preferred constructor for component instantiation
 * <p>
 * Marks a specific constructor as the primary initialization mechanism when
 * multiple constructors exist in a component class. The annotated constructor
 * will be used for dependency injection and instance creation.
 * </p>
 * 
 * <p>Typical use cases:</p>
 * <ul>
 *   <li>Classes with multiple constructors requiring explicit selection</li>
 *   <li>Constructor-based dependency injection scenarios</li>
 *   <li>Non-default constructor initialization requirements</li>
 * </ul>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface DefaultConstructor {
}