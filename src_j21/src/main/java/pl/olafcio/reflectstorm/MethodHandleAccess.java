package pl.olafcio.reflectstorm;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

enum MethodHandleAccess {
    ;

    private static MethodHandles.Lookup IMPL_LOOKUP;

    private static void setup() {
        Unsafe unsafe = UnsafeAccess.unsafe;

        try {
            var f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            var base = unsafe.staticFieldBase(f);
            var offset = unsafe.staticFieldOffset(f);

            IMPL_LOOKUP = (MethodHandles.Lookup) unsafe.getObject(base, offset);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            setup();
        } catch (RuntimeException e) {
            Logging.warn("Failed to load unsafe & IMPL_LOOKUP; accesses might fail");
        }
    }

    public static MethodHandles.Lookup getLookupFor(Class<?> klass) {
        if (IMPL_LOOKUP != null) {
            return IMPL_LOOKUP;
        } else {
            var lookup = MethodHandles.lookup();

            try {
                return MethodHandles.privateLookupIn(klass, lookup);
            } catch (IllegalAccessException e) {
                return lookup;
            }
        }
    }
}
