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

        public void debug(String format, Object... args) {
            if( isDebugEnabled() ) {
                logger.fine( String.format(format, args) );
            }
        }

        public void info(String msg) {
            logger.info(msg);
        }

        public void info(String format, Object... args) {
            if( isInfoEnabled() ) {
                logger.info(String.format(format,args));
            }
        }

        public void error(String msg, Throwable t) {
            logger.log(Level.SEVERE, msg, t);
        }

        public void error(String format, Object... args) {
            logger.severe(String.format(format,args));
        }
    }
}
