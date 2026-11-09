package pl.olafcio.reflectstorm;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public enum UnsafeAccess {
    ;

    public static Unsafe unsafe;

    private static void setup() {
        Class<?> unsafeCls = Unsafe.class;
        try {
            Field field = unsafeCls.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(unsafeCls);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            setup();
        } catch (RuntimeException e) {
            Logging.warn("Failed to load unsafe; accesses might fail");
        }
    }
}
