package org.itech.framework.fx.core.processor.components_processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.itech.framework.fx.core.annotations.ComponentScan;
import org.itech.framework.fx.core.annotations.api_client.EnableApiClient;
import org.itech.framework.fx.core.annotations.components.Component;
import org.itech.framework.fx.core.annotations.components.IgnoreInterfaces;
import org.itech.framework.fx.core.annotations.components.levels.BusinessLogic;
import org.itech.framework.fx.core.annotations.components.levels.DataAccess;
import org.itech.framework.fx.core.annotations.components.levels.Presentation;
import org.itech.framework.fx.core.annotations.components.policy.DisableLoaded;
import org.itech.framework.fx.core.annotations.constructor.DefaultConstructor;
import org.itech.framework.fx.core.annotations.jfx.EnableJavaFx;
import org.itech.framework.fx.core.annotations.methods.InitMethod;
import org.itech.framework.fx.core.annotations.parameters.DefaultParameter;
import org.itech.framework.fx.core.annotations.properties.Property;
import org.itech.framework.fx.core.annotations.reactives.Rx;
import org.itech.framework.fx.core.annotations.storage.DataStorage;
import org.itech.framework.fx.core.module.ComponentInitializer;
import org.itech.framework.fx.core.module.ComponentRegistry;
import org.itech.framework.fx.core.module.ModuleInitializer;
import org.itech.framework.fx.core.resourcecs.CleanupRegistry;
import org.itech.framework.fx.core.storage.DataStorageService;
import org.itech.framework.fx.core.storage.DefaultDataStorageService;
import org.itech.framework.fx.core.store.ComponentStore;
import org.itech.framework.fx.core.utils.ObjectUtils;
import org.itech.framework.fx.core.utils.PackageClassesLoader;
import org.itech.framework.fx.core.utils.PropertiesLoader;
import org.itech.framework.fx.exceptions.FrameworkException;
import org.itech.framework.fx.utils.AnnotationUtils;

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
    private static boolean javaFxEnabled = false;

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

            // registering data storage service
            if (ComponentStore.getComponent(DataStorageService.class.getName()) == null) {
                logger.debug("Registering default StorageService");
                DataStorageService defaultStorage = new DefaultDataStorageService();
                ComponentStore.registerComponent(DataStorageService.class.getName(), defaultStorage, DEFAULT_LEVEL);
            }

            // check for api client component is enabled or not
            if (clazz.isAnnotationPresent(EnableApiClient.class)) {
                apiClientsEnabled = true;
            }

            // check for javafx
            if(clazz.isAnnotationPresent(EnableJavaFx.class)){
                javaFxEnabled = true;
                validateForJavaFx();
            }

            String basePackage = componentScan.basePackage();
            List<Class<?>> classes = PackageClassesLoader.findAllClasses(basePackage, clazz);
            for (Class<?> componentClass : classes) {
                logger.debug("Scanning class: {}", componentClass.getName());
                processComponents(componentClass);
            }

            processTierLevel(DATA_ACCESS_LEVEL);
            processTierLevel(BUSINESS_LOGIC_LEVEL);
            processTierLevel(PRESENTATION_LEVEL);
            processTierLevel(DEFAULT_LEVEL);

            // clean up resources registry
            Runtime.getRuntime().addShutdownHook(new Thread(CleanupRegistry::cleanup));
        }
    }

    private static void validateForJavaFx(){
        if(javaFxEnabled){
            try {
                Class.forName("org.itech.framework.fx.java_fx.ITechJavaFxApplication");
            } catch (ClassNotFoundException e) {
                throw new FrameworkException("""
                            JavaFx is enable but no java fx module found! please add this in your pom or download jar for javafx component.
                            <groupId>org.itech.framework.fx</groupId>
                            <artifactId>java-fx</artifactId>
                        """);
            }
        }
    }

    private static void processTierLevel(int level) {
        ComponentStore.getComponentsByLevel(level).forEach(instance -> {
            if(!AnnotationUtils.hasAnnotation(instance.getClass(), DisableLoaded.class)){
                injectFields(instance.getClass(), instance);
                injectMethods(instance.getClass(), instance);
            }
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

                if(!AnnotationUtils.hasAnnotation(clazz, IgnoreInterfaces.class)){
                    for (Class<?> iface : clazz.getInterfaces()) {
                        String interfaceKey = iface.getName();
                        if (ComponentStore.components.containsKey(interfaceKey)) {
                            throw new IllegalArgumentException("Duplicate component key for interface: " + interfaceKey);
                        }
                        ComponentStore.registerComponent(interfaceKey, instance, level);
                    }
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
            return ObjectUtils.convertValue(defaultParam.value(), parameter.getType());
        }

        return getDefaultValueForType(parameter.getType());
    }

    public static void injectFields(Class<?> clazz, Object instance) {
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
                if (field.isAnnotationPresent(DataStorage.class)) {
                    processDataStorageField(instance, field);
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

        Object convertedValue = ObjectUtils.convertValue(value, field.getType());
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

    private static void processDataStorageField(Object instance, Field field) throws IllegalAccessException {
        DataStorage dataStorage = field.getAnnotation(DataStorage.class);
        String key = dataStorage.key().isEmpty() ? field.getName() : dataStorage.key();

        DataStorageService storageService = (DataStorageService) ComponentStore.getComponent(DataStorageService.class.getName());
        if (storageService == null) {
            throw new IllegalStateException("No StorageService available for @DataStorage");
        }

        String storedValue = storageService.load(key);
        Object valueToSet;

        if (storedValue != null) {
            valueToSet = ObjectUtils.convertValue(storedValue, field.getType());
        } else {
            String defaultValue = dataStorage.defaultValue();
            if (!defaultValue.isEmpty()) {
                valueToSet = ObjectUtils.convertValue(defaultValue, field.getType());
            } else {
                field.setAccessible(true);
                valueToSet = field.get(instance);
            }
        }

        field.setAccessible(true);
        field.set(instance, valueToSet);

        /*CleanupRegistry.addTask(() -> {
            try {
                field.setAccessible(true);
                Object currentValue = field.get(instance);
                String valueToStore = convertToString(currentValue, field.getType());
                storageService.save(key, valueToStore);
            } catch (IllegalAccessException e) {
                logger.error("Failed to save DataStorage field {}", key, e);
            }
        }, DEFAULT_LEVEL);*/
    }

    private static String convertToString(Object value, Class<?> type) {
        if (value == null) return null;
        if (type.isEnum()) {
            return ((Enum<?>) value).name();
        }
        return value.toString();
    }

    public static void injectMethods(Class<?> clazz, Object instance) {
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