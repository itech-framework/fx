package io.github.itech_framework.core.annotations.methods;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for execution prior to component destruction
 * <p>
 * Annotated methods will be automatically invoked during the component teardown phase
 * before the instance is removed from the application context. Typically used for:
 * <ul>
 *   <li>Resource cleanup and release</li>
 *   <li>Connection termination</li>
 *   <li>State persistence operations</li>
 * </ul>
 * </p>
 * 
 * <p>Guaranteed to execute when:</p>
 * <ul>
 *   <li>Application context is shutting down</li>
 *   <li>Component is explicitly removed from context</li>
 *   <li>Garbage collection occurs (context-managed)</li>
 * </ul>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 * @see OnInit
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreDestroy {
}