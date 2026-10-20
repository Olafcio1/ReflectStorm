package pl.olafcio.reflectstorm;

import org.slf4j.LoggerFactory;

enum Logging {
    ;

    public static void warn(String message) {
        try {
            LoggerFactory.getLogger("ReflectStorm")
                         .warn(message);
        } catch (Throwable e) {
            System.err.println("[ReflectStorm] " + message);
        }
    }
}
