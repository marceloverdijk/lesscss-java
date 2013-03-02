package org.lesscss.logging;

interface LessLoggerProvider {
    LessLogger getLogger(Class<?> clazz);
}
