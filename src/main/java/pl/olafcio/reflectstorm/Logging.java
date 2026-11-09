package pl.olafcio.reflectstorm;

enum Logging {
    ;

    public static void warn(String message) {
        try {
            Abstraction.get().warn(message);
        } catch (Throwable e) {
            System.err.println("[ReflectStorm] " + message);
        }
    }
}
