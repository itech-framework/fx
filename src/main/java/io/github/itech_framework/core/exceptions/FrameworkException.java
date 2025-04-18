package io.github.itech_framework.core.exceptions;

public class FrameworkException extends RuntimeException {
    public FrameworkException(String message) {
        super(message + "\nJPA Setup Guide: https://yourframework.com/jpa-setup");
    }

    public FrameworkException(String message, Exception e) {
        super(message + "\nJPA Setup Guide: https://yourframework.com/jpa-setup", e);
    }
}
