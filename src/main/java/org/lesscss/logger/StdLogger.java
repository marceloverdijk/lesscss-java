package org.lesscss.logger;

public class StdLogger implements Logger {
    public void debug(Object message) {
        System.out.println(message.toString());
    }

    public void error(Object message) {
        System.err.println(message.toString());
    }

    public void error(Object message, Throwable exception) {
        System.err.println(String.format("%s: %s", message, exception));
    }
}
