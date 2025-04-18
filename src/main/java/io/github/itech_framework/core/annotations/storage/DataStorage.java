package io.github.itech_framework.core.annotations.storage;

import io.github.itech_framework.core.annotations.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Persists field values using Java Preferences API storage
 * <p>
 * Automatically manages storage and retrieval of field values through the
 * platform's native preferences system. Values are persisted between application
 * executions and maintained according to platform-specific storage rules.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Stores user preference with explicit key
 * @DataStorage(key = "user.theme", defaultValue = "dark")
 * private String interfaceTheme;
 * 
 * // Auto-generated key from field name
 * @DataStorage(defaultValue = "8080")
 * private int serverPort;
 * }
 * </pre>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DataStorage {
    /**
     * Preferences key for storage/retrieval
     * <p>
     * If not specified, the field name will be used as the key.
     * Keys are automatically namespaced to the component's package/class.
     * </p>
     * 
     * @return Custom storage key identifier
     */
    String key() default "";

    /**
     * Initial value when no existing entry is found
     * <p>
     * The value will be converted to the field's type using the framework's
     * conversion system. Supported types include:
     * </p>
     * <ul>
     *   <li>Primitive types and wrappers</li>
     *   <li>Strings</li>
     *   <li>Serializable objects (stored as Base64)</li>
     *   <li>JSON-serializable objects (if JSON support enabled)</li>
     * </ul>
     * 
     * @return Default value as String representation
     */
    String defaultValue() default "";
}