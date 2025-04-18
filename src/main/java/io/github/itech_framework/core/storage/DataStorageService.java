package io.github.itech_framework.core.storage;

public interface DataStorageService {
    String load(String key);
    void save(String key, String value);
}
