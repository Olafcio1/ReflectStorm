package pl.olafcio.reflectstorm.impl_class;

import pl.olafcio.reflectstorm.Abstraction;
import pl.olafcio.reflectstorm.Mode;
import pl.olafcio.reflectstorm.ReflectStorm;
import pl.olafcio.reflectstorm.ReflectStormException;

import java.lang.invoke.MethodHandle;

public abstract class ICTotalHandle extends ICTotalHandleStatic {
    public ICTotalHandle(Class<?> klass) {
        super(klass);
    }

    @Override
    public ReflectStorm.Field field(String name, Class<?> type) {
        try {
            return fieldTotalHandle(name, type);
        } catch (Throwable ex) {
            return super.field(name, type);
        }
    }

    protected ReflectStorm.Field fieldTotalHandle(final String name, final Class<?> type) throws IllegalAccessException, NoSuchFieldException {
        final MethodHandle getter = lookup.findGetter(klass, name, type);
        final MethodHandle setter = lookup.findSetter(klass, name, type);

        final Mode[] modes = new Mode[]{Mode.VARHANDLE};

        return new ReflectStorm.Field() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                try {
                    return (T) getter.invoke(parent);
                } catch (Throwable e) {
                    throw new ReflectStormException(String.format("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", klass.getName(), name, type.getName()), modes);
                }
            }

            @Override
            public void set(Object parent, Object value) {
                try {
                    setter.invoke(parent, value);
                } catch (Throwable e) {
                    throw new ReflectStormException(String.format("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", klass.getName(), name, type.getName()), modes);
                }
            }

            @Override
            public ReflectStorm.Class getDeclaringClass() {
                return Abstraction.get().createClass(klass);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Class<?> getType() {
                return type;
            }
        };
    }
}
