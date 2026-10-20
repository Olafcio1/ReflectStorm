package pl.olafcio.reflectstorm;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ReflectStormException extends RuntimeException {
    public ReflectStormException(String message, Throwable parent) {
        super(message, parent);
    }

    public ReflectStormException(String message, Throwable parent, Mode[] modes) {
        super(message + "\n  > tried: " + tostring(modes), parent);
    }

    private static String tostring(Mode[] modes) {
        return Arrays.stream(modes)
                     .map(mode -> mode.name().toLowerCase())
                     .collect(Collectors.joining(", "));
    }
}
