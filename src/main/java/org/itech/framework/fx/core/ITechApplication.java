package org.itech.framework.fx.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.itech.framework.fx.core.processor.components_processor.ComponentProcessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ITechApplication {
    private static final Logger logger = LogManager.getLogger(ITechApplication.class);
    public static void run(Class<?> clazz){
        try {
            ComponentProcessor.initialize(clazz);
            logger.debug("Components initialized!");
        } catch (Exception e) {
            logger.error("Application fails to start! {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
