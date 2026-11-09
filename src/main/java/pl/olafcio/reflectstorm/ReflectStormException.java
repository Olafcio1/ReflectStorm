package pl.olafcio.reflectstorm;

public class ReflectStormException extends RuntimeException {
    public ReflectStormException(String message) {
        super(message);
    }

    public ReflectStormException(String message, Throwable parent) {
        super(message, parent);
    }

    public ReflectStormException(String message, Mode[] modes) {
        super(message + "\n  > tried: " + tostring(modes));
    }

    public ReflectStormException(String message, Throwable parent, Mode[] modes) {
        super(message + "\n  > tried: " + tostring(modes), parent);
    }

    private static String tostring(Mode[] modes) {
        StringBuilder string = new StringBuilder();

        for (int i = 0; i < modes.length; i++) {
            string.append(modes[i].name().toLowerCase());

            if (i != modes.length - 1)
                string.append(",");
        }

        return string.toString();
    }
}
