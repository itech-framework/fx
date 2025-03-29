package org.itech.framework.fx.core.store;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ComponentStore {
    public static final Map<String, Object> components = new ConcurrentHashMap<>();
    private static final Map<String, Integer> componentLevels = new ConcurrentHashMap<>();

    // Existing methods remain for backward compatibility
    public static void registerComponent(String key, Object instance, int level) {
        components.put(key, instance);
        componentLevels.put(key, level);
    }

    // New type-safe registration method
    public static <T> void registerComponent(Class<T> componentType, T instance, int level) {
        String key = componentType.getName();
        components.put(key, instance);
        componentLevels.put(key, level);
    }

    public static List<Object> getComponentsByLevel(int level) {
        return componentLevels.entrySet().stream()
                .filter(entry -> entry.getValue() == level)
                .map(entry -> components.get(entry.getKey()))
                .collect(Collectors.toList());
    }

    // Type-safe component retrieval
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getComponent(Class<T> componentType) {
        return Optional.ofNullable((T) components.get(componentType.getName()));
    }

    // New helper method for existence check
    public static boolean hasComponent(Class<?> componentType) {
        return components.containsKey(componentType.getName());
    }

    // Original method remains for string-based access
    public static Object getComponent(String key) {
        return components.get(key);
    }
}