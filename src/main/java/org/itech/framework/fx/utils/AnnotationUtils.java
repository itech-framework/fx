package org.itech.framework.fx.utils;

import java.lang.annotation.Annotation;

public class AnnotationUtils {

    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        if (clazz.isAnnotationPresent(annotationType)) {
            return true;
        }

        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(annotationType)) {
                return true;
            }
        }

        return false;
    }
}
