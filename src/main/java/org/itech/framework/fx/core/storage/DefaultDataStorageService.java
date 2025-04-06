package org.itech.framework.fx.core.storage;
import java.util.prefs.Preferences;
public class DefaultDataStorageService implements DataStorageService{
    private final Preferences prefs;

    public DefaultDataStorageService() {
        prefs = Preferences.userNodeForPackage(DefaultDataStorageService.class);
    }

    @Override
    public String load(String key) {
        return prefs.get(key, null);
    }

    @Override
    public void save(String key, String value) {
        if (value == null) {
            prefs.remove(key);
        } else {
            prefs.put(key, value);
        }
    }
}
