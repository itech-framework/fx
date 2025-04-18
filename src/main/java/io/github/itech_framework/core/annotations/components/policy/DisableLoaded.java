package io.github.itech_framework.core.annotations.components.policy;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a component from automatic processing during initialization
 * <p>
 * Marks components that should be skipped during component scanning and
 * auto-registration. Disabled components can be manually enabled and
 * registered with the context at a later stage of application execution.
 * </p>
 * 
 * <p>Typical use cases include:</p>
 * <ul>
 *   <li>Conditionally enabled features/modules</li>
 *   <li>Components requiring manual configuration before activation</li>
 *   <li>Experimental or deprecated functionality</li>
 * </ul>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DisableLoaded {
}