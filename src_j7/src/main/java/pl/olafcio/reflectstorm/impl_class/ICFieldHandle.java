package pl.olafcio.reflectstorm.impl_class;

import pl.olafcio.reflectstorm.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

public abstract class ICFieldHandle extends ICTotalHandle {
    public ICFieldHandle(Class<?> klass) {
        super(klass);
    }

    @Override
    public ReflectStorm.Field field(String name, Class<?> type) {
        Field field;

        try {
            field = klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return super.field(name, type);
        }

        try {
            return fieldHandle(field);
        } catch (IllegalAccessException e) {
            if (UnsafeAccess.unsafe == null)
                return Abstraction.get().createField(field, new Mode[]{Mode.REFLECTION});
            else
                return Abstraction.get().createField(field, new Mode[]{Mode.REFLECTION, Mode.UNSAFE});
        }
    }

    protected ReflectStorm.Field fieldHandle(final Field field) throws IllegalAccessException {
        final MethodHandle getter = lookup.unreflectGetter(field);
        final MethodHandle setter = lookup.unreflectSetter(field);

        final Mode[] modes = (UnsafeAccess.unsafe == null)
                ? new Mode[]{ Mode.VARHANDLE, Mode.REFLECTION }
                : new Mode[]{ Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE };

        return new ReflectStormDecorators.Field(Abstraction.get().createField(field, modes)) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                try {
                    if (parent == null)
                        return (T) getter.invoke();
                    else
                        return (T) getter.invoke(parent);
                } catch (Throwable e) {
                    throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", field), e, modes);
                }
            }

            @Override
            public void set(Object parent, Object value) {
                try {
                    if (parent == null)
                        setter.invoke(value);
                    else
                        setter.invoke(parent, value);
                } catch (Throwable e) {
                    try {
                        field.set(parent, value);
                    } catch (IllegalAccessException e1) {
                        if (UnsafeAccess.unsafe == null)
                            throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", field), e1, modes);

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
                            throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", field), ex, modes);
                        }
                    }
                }
            }
        };
    }
}
