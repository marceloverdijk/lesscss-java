package org.lesscss.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonsLogger implements Logger {

    private final Log log;

    public CommonsLogger(Class<?> loggingClass) {
        log = LogFactory.getLog(loggingClass);
    }

    public void debug(Object message) {
        log.debug(message);
    }

    public void error(Object message) {
        log.error(message);
    }

    public void error(Object message, Throwable exception) {
        log.error(message, exception);
    }
}
