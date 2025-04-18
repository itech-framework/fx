package io.github.itech_framework.core.module;

import io.github.itech_framework.core.store.ComponentStore;

public class ComponentRegistry {
    public void registerComponent(String name, Object component, int level) {
        ComponentStore.registerComponent(name, component, level);
    }

    // Type-safe generic registration (recommended)
    public <T> void registerComponent(Class<T> componentType, T componentInstance, int level) {
        validateComponentType(componentType, componentInstance);
        ComponentStore.registerComponent(componentType.getName(), componentInstance, level);
    }

    // Auto-type registration using instance's class
    public void registerComponent(Object componentInstance, int level) {
        Class<?> componentType = componentInstance.getClass();
        ComponentStore.registerComponent(componentType.getName(), componentInstance, level);
    }

    public <T> void registerComponentAsInterface(Class<T> interfaceType, T implementation, int level) {
        validateInterface(interfaceType);
        ComponentStore.registerComponent(interfaceType.getName(), implementation, level);
    }

    private <T> void validateComponentType(Class<T> expectedType, Object component) {
        if (!expectedType.isInstance(component)) {
            throw new IllegalArgumentException(
                    String.format("Component type mismatch. Expected %s but got %s",
                            expectedType.getName(),
                            component.getClass().getName())
            );
        }
    }

    private <T> void validateInterface(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException(
                    String.format("Registration as interface failed: %s is not an interface",
                            type.getName())
            );
        }
    }
}
