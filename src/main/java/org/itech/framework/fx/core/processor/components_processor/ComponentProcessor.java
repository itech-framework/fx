package org.itech.framework.fx.core.processor.components_processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.itech.framework.fx.core.annotations.ComponentScan;
import org.itech.framework.fx.core.annotations.api_client.EnableApiClient;
import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.components.levels.BusinessLogic;
import org.itech.framework.fx.core.annotations.components.levels.DataAccess;
import org.itech.framework.fx.core.annotations.components.levels.Presentation;
import org.itech.framework.fx.core.annotations.constructor.DefaultConstructor;
import org.itech.framework.fx.core.annotations.methods.InitMethod;
import org.itech.framework.fx.core.annotations.parameters.DefaultParameter;
import org.itech.framework.fx.core.annotations.persistences.EnableJPA;
import org.itech.framework.fx.core.annotations.properties.Property;
import org.itech.framework.fx.core.annotations.reactives.Rx;
import org.itech.framework.fx.core.module.ComponentInitializer;
import org.itech.framework.fx.core.module.ComponentRegistry;
import org.itech.framework.fx.core.module.ModuleInitializer;
import org.itech.framework.fx.core.store.ComponentStore;
import org.itech.framework.fx.core.utils.PackageClassesLoader;
import org.itech.framework.fx.core.utils.PropertiesLoader;
import org.itech.framework.fx.exceptions.FrameworkException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ComponentProcessor {
    public static final int DATA_ACCESS_LEVEL = 0;
    public static final int BUSINESS_LOGIC_LEVEL = 1;
    public static final int PRESENTATION_LEVEL = 2;
    public static final int DEFAULT_LEVEL = 3;
    public static final Set<String> JPA_ENTITY_ANNOTATIONS = Set.of(
            "javax.persistence.Entity",
            "jakarta.persistence.Entity"
    );
    private static boolean apiClientsEnabled = false;

    private static final Logger logger = LogManager.getLogger(ComponentProcessor.class);

    public static void initialize(Class<?> clazz) throws Exception {
        logger.debug("Initializing component...");
        logger.debug("Component scan found? -> {}", clazz.isAnnotationPresent(ComponentScan.class));
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);

            // Load properties first
            PropertiesLoader.load(componentScan.properties(), clazz);

            // Load module initializers
            ServiceLoader<ModuleInitializer> loader = ServiceLoader.load(ModuleInitializer.class);
            ComponentRegistry registry = new ComponentRegistry();
            for (ModuleInitializer initializer : loader) {
                initializer.initialize(registry);
            }

            // check for api client component is enabled or not
            if (clazz.isAnnotationPresent(EnableApiClient.class)) {
                apiClientsEnabled = true;
            }

            String basePackage = componentScan.basePackage();
            List<Class<?>> classes = PackageClassesLoader.findAllClasses(basePackage, clazz);

            for (Class<?> componentClass : classes) {
                logger.debug("Scanning class: {}", componentClass.getName());
                processComponents(componentClass);
            }

            // Phase 2: Dependency injection and initialization
            processTierLevel(DATA_ACCESS_LEVEL);
            processTierLevel(BUSINESS_LOGIC_LEVEL);
            processTierLevel(PRESENTATION_LEVEL);
            processTierLevel(DEFAULT_LEVEL);
        }
    }

    private static void processTierLevel(int level) {
        ComponentStore.getComponentsByLevel(level).forEach(instance -> {
            injectFields(instance.getClass(), instance);
            injectMethods(instance.getClass(), instance);
        });
    }


    private static void processComponents(Class<?> clazz) {
        if (isJpaEntity(clazz)) {
            if (!isJpaModuleAvailable()) {
                StringBuilder error = new StringBuilder("JPA required but not available.\n");
                try {
                    Class.forName("org.itech.framework.fx.jpa.config.FlexiJpaConfig");
                    error.append("- JPA module found but initialization failed\n");
                    error.append("- Check your jpa.* properties configuration");
                } catch (ClassNotFoundException e) {
                    error.append("- Add JPA dependency to pom.xml\n");
                    error.append("- Required for entities: ").append(clazz.getName());
                }
                throw new FrameworkException(error.toString());
            }
        }
        if (apiClientsEnabled){
            // process for api client
            // load component initializer
            ServiceLoader<ComponentInitializer> componentInitializerServiceLoader = ServiceLoader.load(ComponentInitializer.class);

            for(ComponentInitializer initializer: componentInitializerServiceLoader){
                initializer.initializeComponent(clazz);
            }
        }
        if (isComponentClass(clazz)) {
            try {
                Component componentAnnotation = getComponentAnnotation(clazz);
                String key = getComponentKey(componentAnnotation, clazz);
                int level = determineComponentLevel(clazz);

                if (ComponentStore.components.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate component key: " + key);
                }

                Constructor<?> constructor = findSuitableConstructor(clazz);
                Object instance = createInstance(constructor);

                ComponentStore.registerComponent(key, instance, level);

                for (Class<?> iface : clazz.getInterfaces()) {
                    String interfaceKey = iface.getName();
                    if (ComponentStore.components.containsKey(interfaceKey)) {
                        throw new IllegalArgumentException("Duplicate component key for interface: " + interfaceKey);
                    }
                    ComponentStore.registerComponent(interfaceKey, instance, level);
                }
            } catch (Exception e) {
                logger.error("Component processing failed", e);
                throw new RuntimeException(e);
            }
        }

    }

    private static boolean isJpaModuleAvailable() {
        try {
            Class<?> configClass = Class.forName("org.itech.framework.fx.jpa.config.FlexiJpaConfig");
            Object config = ComponentStore.getComponent(configClass.getName());

            logger.debug("Is JPA config is NULL : {}", config==null);

            if (config == null) return false;

            Method isInitialized = configClass.getMethod("isInitialized");

            logger.debug("Is JPA initialized correctly: {}", isInitialized.invoke(config));
            return (boolean) isInitialized.invoke(config);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isJpaEntity(Class<?> clazz) {
        return Arrays.stream(clazz.getAnnotations())
                .anyMatch(annotation ->
                        JPA_ENTITY_ANNOTATIONS.contains(annotation.annotationType().getName())
                );
    }

    private static Component getComponentAnnotation(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return clazz.getAnnotation(Component.class);
        }

        for (Annotation annotation : clazz.getAnnotations()) {
            Component component = annotation.annotationType().getAnnotation(Component.class);
            if (component != null) {
                return component;
            }
        }
        throw new IllegalStateException("No component annotation found on " + clazz.getName());
    }

    private static boolean isComponentClass(Class<?> clazz) {
        boolean isComponent = clazz.isAnnotationPresent(Component.class) ||
                Arrays.stream(clazz.getAnnotations())
                        .anyMatch(annotation -> {
                            boolean hasComponent = annotation.annotationType()
                                    .isAnnotationPresent(Component.class);
                            logger.debug("Checking annotation {} on {}: {}",
                                    annotation.annotationType().getSimpleName(),
                                    clazz.getSimpleName(),
                                    hasComponent);
                            return hasComponent;
                        });

        logger.debug("Class {} is component: {}", clazz.getSimpleName(), isComponent);
        return isComponent;
    }

    private static int determineComponentLevel(Class<?> clazz) {
        if (clazz.isAnnotationPresent(DataAccess.class)) return DATA_ACCESS_LEVEL;
        if (clazz.isAnnotationPresent(BusinessLogic.class)) return BUSINESS_LOGIC_LEVEL;
        if (clazz.isAnnotationPresent(Presentation.class)) return PRESENTATION_LEVEL;
        return DEFAULT_LEVEL;
    }

    private static String getComponentKey(Component component, Class<?> clazz) {
        return component.name().isEmpty() ? clazz.getName() : component.name();
    }

    private static Constructor<?> findSuitableConstructor(Class<?> clazz) throws NoSuchMethodException {
        List<Constructor<?>> constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .sorted((c1, c2) -> Integer.compare(c2.getParameterCount(), c1.getParameterCount()))
                .toList();

        Optional<Constructor<?>> defaultConstructor = constructors.stream()
                .filter(c -> c.isAnnotationPresent(DefaultConstructor.class))
                .findFirst();

        if (defaultConstructor.isPresent()) {
            Constructor<?> cons = defaultConstructor.get();
            cons.setAccessible(true);
            return cons;
        }

        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ignored) {}

        for (Constructor<?> constructor : constructors) {
            if (canResolveConstructorParameters(constructor)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }

        throw new NoSuchMethodException("No resolvable constructor found for " + clazz.getName());
    }

    private static boolean canResolveConstructorParameters(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .allMatch(param ->
                        ComponentStore.components.containsKey(param.getType().getName()) ||
                                param.isAnnotationPresent(DefaultParameter.class)
                );
    }

    private static Object createInstance(Constructor<?> constructor) throws Exception {
        Object[] args = Arrays.stream(constructor.getParameters())
                .map(ComponentProcessor::resolveParameter)
                .toArray();
        return constructor.newInstance(args);
    }

    private static Object resolveParameter(Parameter parameter) {
        Object component = ComponentStore.components.get(parameter.getType().getName());
        if (component != null) return component;

        DefaultParameter defaultParam = parameter.getAnnotation(DefaultParameter.class);
        if (defaultParam != null) {
            return convertValue(defaultParam.value(), parameter.getType());
        }

        return getDefaultValueForType(parameter.getType());
    }

    private static void injectFields(Class<?> clazz, Object instance) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            processClassFields(currentClass, instance);
            currentClass = currentClass.getSuperclass();
        }
    }

    private static void processClassFields(Class<?> clazz, Object instance) {
        for (Field field : clazz.getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(Property.class)) {
                    processPropertyField(instance, field);
                }
                if (field.isAnnotationPresent(Rx.class)) {
                    processRxField(instance, field);
                }
            } catch (IllegalAccessException e) {
                logger.error("Field injection failed for {} in {}", field.getName(), clazz.getName(), e);
                throw new RuntimeException("Field injection failed", e);
            }
        }
    }

    private static void processPropertyField(Object instance, Field field) throws IllegalAccessException {
        Property property = field.getAnnotation(Property.class);
        String key = property.key();
        String defaultValue = property.defaultValue();

        String value = PropertiesLoader.getProperty(key, defaultValue);

        if (value == null || value.isEmpty()) {
            if (defaultValue.isEmpty()) {
                throw new IllegalStateException("Property '" + key + "' not found and no default value specified");
            }
            value = defaultValue;
        }

        Object convertedValue = convertValue(value, field.getType());
        field.setAccessible(true);
        field.set(instance, convertedValue);
    }

    private static void processRxField(Object instance, Field field) throws IllegalAccessException {
        Rx rx = field.getAnnotation(Rx.class);
        String key = rx.name().isEmpty() ? field.getType().getName() : rx.name();

        Object component = ComponentStore.components.get(key);
        if (component == null) {
            throw new IllegalStateException("Missing component for key: " + key);
        }

        if (!field.getType().isAssignableFrom(component.getClass())) {
            throw new ClassCastException("Component type mismatch for field " + field.getName());
        }

        field.setAccessible(true);
        field.set(instance, component);
    }

    private static void injectMethods(Class<?> clazz, Object instance) {
        List<Method> initMethods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(InitMethod.class))
                .sorted(Comparator.comparingInt(m -> m.getAnnotation(InitMethod.class).order()))
                .toList();

        for (Method method : initMethods) {
            validateInitMethod(method);
            try {
                Object[] params = resolveMethodParameters(method);
                method.setAccessible(true);
                method.invoke(instance, params);
            } catch (Exception e) {
                logger.error("Init method failed: {}", method.getName(), e);
                throw new RuntimeException("Init method execution failed", e);
            }
        }
    }

    private static void validateInitMethod(Method method) {
        if (method.getParameterCount() > 0 && !canResolveMethodParameters(method)) {
            throw new IllegalStateException("Unresolvable parameters for method: " + method.getName());
        }
    }

    private static boolean canResolveMethodParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .allMatch(param ->
                        ComponentStore.components.containsKey(param.getType().getName()) ||
                                param.isAnnotationPresent(DefaultParameter.class)
                );
    }

    private static Object[] resolveMethodParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .map(param -> {
                    Object component = ComponentStore.components.get(param.getType().getName());
                    if (component != null) return component;
                    throw new IllegalStateException("Cannot resolve parameter: " + param.getName());
                })
                .toArray();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertValue(String value, Class<?> type) {
        if (type == String.class) return value;

        try {
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(value);
            } else if (type == double.class || type == Double.class) {
                return Double.parseDouble(value);
            } else if (type == long.class || type == Long.class) {
                return Long.parseLong(value);
            } else if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (type == float.class || type == Float.class) {
                return Float.parseFloat(value);
            } else if (type == short.class || type == Short.class) {
                return Short.parseShort(value);
            } else if (type == byte.class || type == Byte.class) {
                return Byte.parseByte(value);
            } else if (type == char.class || type == Character.class) {
                if (value.length() != 1) {
                    throw new IllegalArgumentException("Char value must be exactly 1 character");
                }
                return value.charAt(0);
            } else if (type.isEnum()) {
                return Enum.valueOf((Class<Enum>) type, value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot convert value '" + value + "' to type " + type.getSimpleName(), e
            );
        }

        throw new UnsupportedOperationException(
                "Unsupported conversion to type: " + type.getName()
        );
    }

    private static Object getDefaultValueForType(Class<?> type) {
        if (type == int.class) return 0;
        if (type == double.class) return 0.0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == Integer.class || type == Double.class ||
                type == Long.class || type == Float.class ||
                type == Short.class || type == Byte.class ||
                type == Boolean.class || type == Character.class ||
                type == String.class) return null;

        throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
    }
}