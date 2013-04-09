package org.lesscss.logging;

public class LessLoggerFactory {
    private static LessLoggerFactory instance = new LessLoggerFactory();
    private LessLoggerProvider loggerProvider;

    private LessLoggerFactory() {
        try {
            Class.forName("org.slf4j.Logger");
            loggerProvider = new SLF4JLessLoggerProvider();
        } catch(ClassNotFoundException ex) {
            loggerProvider = new JULILessLoggerProvider();
        }
    }

    public static LessLogger getLogger(Class<?> clazz) {
        return instance.loggerProvider.getLogger(clazz);
    }
}
