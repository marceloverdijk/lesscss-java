package org.lesscss.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

class JULILessLoggerProvider implements LessLoggerProvider {
    public LessLogger getLogger(Class<?> clazz) {
        return new JULILessLogger(Logger.getLogger(clazz.getName()));
    }

    private static class JULILessLogger implements LessLogger {
        private final Logger logger;

        private JULILessLogger(Logger logger) {
            this.logger = logger;
        }

        public boolean isDebugEnabled() {
            return logger.isLoggable(Level.FINE);
        }

        public boolean isInfoEnabled() {
            return logger.isLoggable(Level.INFO);
        }

        public void debug(String msg) {
            logger.fine(msg);
        }

        public void info(String msg) {
            logger.info(msg);
        }

        public void error(String msg, Throwable t) {
            logger.log(Level.SEVERE, msg, t);
        }
    }
}
