package io.github.itech_framework.core.annotations.jfx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables JavaFX integration and context configuration
 * <p>
 * Marks an application class to activate JavaFX framework support, typically used for:
 * <ul>
 *   <li>Initializing the JavaFX application thread</li>
 *   <li>Configuring dependency injection for JavaFX components</li>
 *   <li>Enabling automatic FXML loading and controller wiring</li>
 * </ul>
 * Required for applications using JavaFX UI components and features.
 * </p>
 *
 * @author Sai Zaw Myint
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnableJavaFx {
}