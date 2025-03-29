package org.itech.framework.fx.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final Properties properties = new Properties();

    public static void load(String[] filenames, Class<?> clazz) {
        if(filenames ==null) return;
        for(String filename: filenames){
            try (InputStream input = clazz.getClassLoader()
                    .getResourceAsStream(filename)) {

                if (input == null) {
                    throw new RuntimeException("Properties file not found: " + filename);
                }
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Error loading properties file", e);
            }
        }

    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
