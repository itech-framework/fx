package io.github.itech_framework.core.resourcecs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CleanupRegistry {
    private static final Logger logger = LogManager.getLogger(CleanupRegistry.class);
    private static final List<TaskWithPriority> tasks = new ArrayList<>();

    public static void register(Runnable task, int priority) {
        tasks.add(new TaskWithPriority(task, priority));
        tasks.sort(Comparator.comparingInt(TaskWithPriority::priority));
    }

    public static void addTask(Runnable task, int priority) {
        tasks.add(new TaskWithPriority(task, priority));
    }


    public static void cleanup() {
        logger.info("Starting resource cleanup...");
        for (TaskWithPriority twp : tasks) {
            try {
                logger.debug("Executing cleanup task with priority: {}", twp.priority());
                twp.task().run();
            } catch (Exception e) {
                logger.error("Cleanup task failed", e);
            }
        }
        logger.info("Resource cleanup completed.");
    }

    private record TaskWithPriority(Runnable task, int priority) {}
}