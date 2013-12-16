package org.lesscss.logging;

class SLF4JLessLoggerProvider implements LessLoggerProvider {
    public LessLogger getLogger(Class<?> clazz) {
        return new SLF4JLessLogger(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    private static class SLF4JLessLogger implements LessLogger {
        private final org.slf4j.Logger logger;

        private SLF4JLessLogger(org.slf4j.Logger logger) {
            this.logger = logger;
        }

        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        public void debug(String msg) {
            logger.debug(msg);
        }

        public void debug(String format, Object... args) {
            if( logger.isDebugEnabled() ) {
                logger.debug(String.format(format, args));
            }
        }

        public void info(String msg) {
            logger.info(msg);
        }

        public void info(String format, Object... args) {
            if( logger.isInfoEnabled() ) {
                logger.info( String.format(format, args) );
            }
        }

        public void error(String msg, Throwable t) {
            logger.error(msg, t);
        }

        public void error(String format, Object... args) {
            if( logger.isErrorEnabled() ) {
                logger.error( String.format(format, args) );
            }
        }

    }
}
