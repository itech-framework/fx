package io.github.itech_framework.core.annotations.components.levels;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a data access component (repository layer)
 * <p>
 * Identifies classes that handle database operations and data persistence logic.
 * Specialization of {@link Component} that typically contains CRUD operations,
 * query execution, and transaction management related to data storage.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface DataAccess {
}