package org.itech.framework.fx.core.processor.components_processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.itech.framework.fx.core.annotations.ComponentScan;
import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.constructor.DefaultConstructor;
import org.itech.framework.fx.core.annotations.methods.InitMethod;
import org.itech.framework.fx.core.annotations.parameters.DefaultParameter;
import org.itech.framework.fx.core.annotations.reactives.Rx;
import org.itech.framework.fx.core.store.ComponentStore;
import org.itech.framework.fx.core.utils.PackageClassesLoader;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ComponentProcessor {

    private static final Logger logger = LogManager.getLogger(ComponentProcessor.class);

    public static void initialize(Class<?> clazz) throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        logger.debug("Initializing component...");
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
            String basePackage = componentScan.basePackage();
            List<Class<?>> classes = PackageClassesLoader.findAllClasses(basePackage);
            for (Class<?> clzz : classes) {
                processComponents(clzz);
            }

            for(Object object: ComponentStore.components.values()){
                injectFields(object.getClass(), object);
                injectMethods(object.getClass(), object);
            }
        }
    }

    private static void processComponents(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            try {
                Component component = clazz.getAnnotation(Component.class);
                String key = getComponentKey(component, clazz);

                Constructor<?> constructor = findSuitableConstructor(clazz);
                Object instance = createInstance(constructor);

                if (ComponentStore.components.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate component key " + key + "!");
                } else {
                    ComponentStore.components.put(key, instance);
                }
            } catch (Exception e) {
                logger.error("Failed to process component {}", clazz.getName(), e);
            }
        }
    }

    private static String getComponentKey(Component component, Class<?> clazz) {
        return component.name().isEmpty() ? clazz.getName() : component.name();
    }
    private static Constructor<?> findSuitableConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> suitableConstructor = null;

        for (Constructor<?> cons : constructors) {
            if(cons.isAnnotationPresent(DefaultConstructor.class)){
                suitableConstructor = cons;
                break;
            }
            if (suitableConstructor == null || cons.getParameterCount() > suitableConstructor.getParameterCount()) {
                suitableConstructor = cons;
            }
        }

        if (suitableConstructor == null) {
            throw new NoSuchMethodException("No suitable constructor found for class " + clazz.getName());
        }

        suitableConstructor.setAccessible(true);
        return suitableConstructor;
    }

    private static Object createInstance(Constructor<?> constructor) throws Exception {
        Object[] args = createConstructorArguments(constructor);
        return constructor.newInstance(args);
    }
    private static Object[] createConstructorArguments(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            DefaultParameter defaultValue = parameter.getAnnotation(DefaultParameter.class);

            if (defaultValue != null) {
                args[i] = convertValue(defaultValue.value(), parameter.getType());
            }else{
                args[i] = getDefaultValueForType(parameter.getType());
            }
        }
        return args;
    }
    private static Object convertValue(String value, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        } else if (type == byte.class || type == Byte.class) {
            return Byte.parseByte(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == char.class || type == Character.class) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Invalid char value: " + value);
            }
            return value.charAt(0);
        } else if (type == String.class) {
            return value;
        }
        throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
    }
    private static Object getDefaultValueForType(Class<?> type) {
        if (type == int.class) {
            return 0;
        } else if (type == double.class) {
            return 0.0;
        } else if (type == long.class) {
            return 0L;
        } else if (type == float.class) {
            return 0.0f;
        } else if (type == short.class) {
            return (short) 0;
        } else if (type == byte.class) {
            return (byte) 0;
        } else if (type == boolean.class) {
            return false;
        } else if (type == char.class) {
            return '\0'; // Default char value
        } else if (type == Integer.class || type == Double.class || type == Long.class || type == Float.class || type == Short.class || type == Byte.class || type == Boolean.class || type == Character.class || type == String.class) {
            return null; // Wrapper types and String can be null if not provided
        }
        throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
    }

    private static void injectFields(Class<?> clazz, Object instance) {
        processFieldLevel(clazz, instance);
    }

    private static void injectMethods(Class<?> clazz, Object instance){
        Method[] methods = clazz.getDeclaredMethods();
        List<Method> initMethods = Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(InitMethod.class))
                .sorted(Comparator.comparingInt(method -> method.getAnnotation(InitMethod.class).order()))
                .toList();
        for (Method method : initMethods) {
            method.setAccessible(true);
            try {
                method.invoke(instance);
            } catch (Exception e) {
                System.err.println("Failed to execute method " + method.getName());
                e.printStackTrace();
            }
        }
    }
    private static Object findInstance(Class<?> clazz){
        String name = "";
        if(clazz.isAnnotationPresent(Component.class)){
            Component component = clazz.getAnnotation(Component.class);
            name = component.name();
        }else{
            logger.warn("The class is not the component class, no instance can be pares!");
            return null;
        }
        return ComponentStore.components.get(name);
    }
    private static void processFieldLevel(Class<?> clazz, Object instance) {
        for (Field field : clazz.getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Rx.class)) {
                    processRx(instance,field);
                }
            } catch (IllegalAccessException | IllegalArgumentException e) {
                logger.error("Failed to set field {} in class {}", field.getName(), clazz.getName(), e);
            }
        }
    }

    private static void processRx(Object instance,Field field) throws IllegalAccessException {
        Rx rx = field.getAnnotation(Rx.class);
        field.setAccessible(true);
        String key = rx.name();
        Object object = ComponentStore.components.get(key);
        if (object == null) {
            logger.warn("No component found in store with key {}", key);
            return;
        }
        if (field.getType().isInstance(object)) {
            field.set(instance, object);
        } else {
            throw new IllegalArgumentException("Component with key " + key + " is not of the expected type.");
        }
    }
}
