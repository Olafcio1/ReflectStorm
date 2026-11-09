package pl.olafcio.reflectstorm;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public enum MethodHandleAccess {
    ;

    private static MethodHandles.Lookup IMPL_LOOKUP;

    private static void setup() {
        Unsafe unsafe = UnsafeAccess.unsafe;

        try {
            final Field f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            final Object base = unsafe.staticFieldBase(f);
            final long offset = unsafe.staticFieldOffset(f);

            IMPL_LOOKUP = (MethodHandles.Lookup) unsafe.getObject(base, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Constructor<MethodHandles.Lookup> lookupConstructor;

    private static void setup2() {
        try {
            lookupConstructor =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);

            lookupConstructor.setAccessible(true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            setup();
        } catch (RuntimeException e) {
            Logging.warn("Failed to load IMPL_LOOKUP; accesses might fail");
        }

        try {
            setup2();
        } catch (RuntimeException e) {
            Logging.warn("Failed to retrieve MethodHandles.Lookup(Class<?>, int) constructor; accesses might fail");
        }
    }

    public static MethodHandles.Lookup getLookupFor(Class<?> klass) {
        if (IMPL_LOOKUP != null) {
            return IMPL_LOOKUP;
        } else {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            try {
                return lookupConstructor.newInstance(klass, -1 /* MethodHandles.Lookup::TRUSTED */);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                return lookup;
            }
        }
    }
}
