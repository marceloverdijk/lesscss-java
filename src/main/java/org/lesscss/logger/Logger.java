package org.lesscss.logger;

public interface Logger {
    void debug(Object message);
    void error(Object message);
    void error(Object message, Throwable exception);
}
