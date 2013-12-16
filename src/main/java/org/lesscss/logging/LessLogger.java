package org.lesscss.logging;

public interface LessLogger {
    boolean isDebugEnabled();

    boolean isInfoEnabled();

    void debug(String msg);

    void debug(String format, Object... args);

    void info(String msg);

    void info(String format, Object... args);

    void error(String msg, Throwable t);

    void error(String format, Object... args);

}
