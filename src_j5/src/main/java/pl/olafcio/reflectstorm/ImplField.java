package pl.olafcio.reflectstorm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static pl.olafcio.reflectstorm.Util.fillout;

class ImplField implements ReflectStorm.Field {
    private final Field field;
    private final Mode[] modes;

    private static final Field MODIFIERS;

    static {
        Field val;

        try {
            val = Field.class.getDeclaredField("modifiers");
            val.setAccessible(true);
        } catch (Throwable e) {
            val = null;

            Logging.warn("Failed to retrieve Field::modifiers; accesses might fail");
        }

        MODIFIERS = val;
    }

    public ImplField(Field field, Mode[] modes) {
        this.field = field;
        this.modes = modes;

        try {
            field.setAccessible(true);

            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)
                MODIFIERS.set(field, field.getModifiers() - Modifier.FINAL);
        } catch (Throwable ignored) {
        }
    }

    public Class<?> getType() {
        return field.getType();
    }

    public String getName() {
        return field.getName();
    }

    public ReflectStorm.Class getDeclaringClass() {
        return Abstraction.get().createClass(field.getDeclaringClass());
    }

    public void set(Object parent, Object value) {
        try {
            field.set(parent, value);
        } catch (IllegalAccessException e) {
            if (UnsafeAccess.unsafe == null)
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", field), e, modes);

            try {
                if (parent != null) {
                    UnsafeAccess.unsafe.putObject(
                            parent,
                            UnsafeAccess.unsafe.objectFieldOffset(field),
                            value
                    );
                } else {
                    UnsafeAccess.unsafe.putObject(
                            UnsafeAccess.unsafe.staticFieldBase(field),
                            UnsafeAccess.unsafe.staticFieldOffset(field),
                            value
                    );
                }
            } catch (Throwable ex) {
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", field), ex, modes);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object parent) {
        try {
            return (T) field.get(parent);
        } catch (IllegalAccessException e) {
            if (UnsafeAccess.unsafe == null)
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", field), e, modes);

            try {
                if (parent != null) {
                    return (T) UnsafeAccess.unsafe.getObject(
                            parent,
                            UnsafeAccess.unsafe.objectFieldOffset(field)
                    );
                } else {
                    return (T) UnsafeAccess.unsafe.getObject(
                            UnsafeAccess.unsafe.staticFieldBase(field),
                            UnsafeAccess.unsafe.staticFieldOffset(field)
                    );
                }
            } catch (Throwable ex) {
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", field), ex, modes);
            }
        }
    }
}
