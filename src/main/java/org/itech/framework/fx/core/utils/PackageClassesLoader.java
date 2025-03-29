package org.itech.framework.fx.core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PackageClassesLoader {
    private static final Logger logger = LogManager.getLogger(PackageClassesLoader.class);

    public static List<Class<?>> findAllClasses(String basePackage, Class<?> clazz)
            throws IOException {

        ClassLoader classLoader = clazz.getClassLoader();
        String path = basePackage.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        logger.debug("Scanning package: {}", basePackage);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                try {
                    String decodedPath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                    File dir = new File(decodedPath);
                    if (dir.exists() && dir.isDirectory()) {
                        dirs.add(dir);
                        logger.debug("Found directory: {}", dir.getAbsolutePath());
                    }
                } catch (Exception e) {
                    logger.error("Error processing resource: {}", resource, e);
                }
            }
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, basePackage, classLoader));
        }

        logger.info("Found {} classes in package {}", classes.size(), basePackage);
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName, ClassLoader classLoader) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            logger.warn("Directory does not exist: {}", directory.getAbsolutePath());
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) return classes;

        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file,
                            packageName + "." + file.getName(),
                            classLoader));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' +
                            file.getName().substring(0, file.getName().length() - 6);
                    classes.add(loadClass(className, classLoader));
                }
            } catch (Exception e) {
                logger.warn("Skipping file {}: {}", file.getName(), e.getMessage());
            }
        }
        return classes;
    }

    private static Class<?> loadClass(String className, ClassLoader classLoader)
            throws ClassNotFoundException {
        try {
            return classLoader.loadClass(className);
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException("Dependency missing for class: " + className, e);
        }
    }
}