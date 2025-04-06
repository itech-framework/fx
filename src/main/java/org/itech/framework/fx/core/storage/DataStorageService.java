package org.itech.framework.fx.core.storage;

public interface DataStorageService {
    String load(String key);
    void save(String key, String value);
}
