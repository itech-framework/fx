package io.github.itech_framework.core.annotations;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring component scanning and property file locations
 * <p>
 * Used to define the base package for component discovery and specify property files
 * to be loaded by the framework. This annotation should be applied to the main
 * application class.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScan {
    /**
     * Base package to scan for framework components
     * <p>
     * The framework will recursively scan this package and its sub-packages
     * for classes annotated with component annotations like {@link Component}.
     * </p>
     * @return The root package name for component scanning
     */
    String basePackage();

    /**
     * Property files to load for application configuration
     * <p>
     * Specifies one or more property files (classpath-relative) to be loaded
     * by the framework. Files are loaded in array order, with later files
     * overriding values from earlier ones.
     * </p>
     * <p>Default: {@code {"application.properties"}}</p>
     * 
     * @return Array of property file locations
     */
    String[] properties() default {"application.properties"};
}