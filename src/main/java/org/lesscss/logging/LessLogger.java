package org.lesscss.logging;

public interface LessLogger {
    boolean isDebugEnabled();

    boolean isInfoEnabled();

    void debug(String msg);

    void info(String msg);

    void error(String msg, Throwable t);
}
