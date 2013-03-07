package org.lesscss.logger;

public class LastMessageLogger implements Logger {
    private String lastDebug;
    private String lastError;
    private Throwable lastException;

    public void debug(Object message) {
        lastDebug = message.toString();
    }

    public void error(Object message) {
        lastError = message.toString();
    }

    public void error(Object message, Throwable exception) {
        error(message);
        lastException = exception;
    }

    public String lastDebug() {
        return lastDebug;
    }

    public String lastError() {
        return lastError;
    }

    public Throwable lastException() {
        return lastException;
    }
}
