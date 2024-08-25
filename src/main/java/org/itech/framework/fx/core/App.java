package org.itech.framework.fx.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.itech.framework.fx.core.processor.components_processor.ComponentProcessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App{
    private static final Logger logger = LogManager.getLogger(App.class);
    public static void run(Class<?> clazz){
        try {
            ComponentProcessor.initialize(clazz);
            logger.debug("Components initialized!");
        } catch (IOException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            logger.error("Application fails to start! {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
