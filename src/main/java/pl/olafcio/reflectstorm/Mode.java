package pl.olafcio.reflectstorm;

/**
 * The reflection type to be performed.
 */
public enum Mode {
    /**
     * The {@link java.lang.invoke.MethodHandles MethodHandles} API, available since Java 1.7.
     */
    VARHANDLE,

    /**
     * Standard reflection.
     */
    REFLECTION,

    /**
     * The internal, proprietary {@link sun.misc.Unsafe Unsafe} API.
     */
    UNSAFE
}
