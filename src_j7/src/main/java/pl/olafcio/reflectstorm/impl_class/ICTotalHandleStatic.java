package pl.olafcio.reflectstorm.impl_class;

import pl.olafcio.reflectstorm.*;
import pl.olafcio.reflectstorm.base.BaseClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public abstract class ICTotalHandleStatic extends BaseClass {
    protected final MethodHandles.Lookup lookup;

    public ICTotalHandleStatic(Class<?> klass) {
        super(klass);
        this.lookup = MethodHandleAccess.getLookupFor(klass);
    }

    @Override
    public ReflectStorm.Field field(String name, Class<?> type) {
        try {
            return fieldTotalHandleStatic(name, type);
        } catch (Throwable exc) {
            throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to get field '%s#%s[%s]'", klass, name, type), exc);
        }
    }

    protected ReflectStorm.Field fieldTotalHandleStatic(final String name, final Class<?> type) throws IllegalAccessException, NoSuchFieldException {
        final MethodHandle getter = lookup.findStaticGetter(klass, name, type);
        final MethodHandle setter = lookup.findStaticSetter(klass, name, type);

        final Mode[] modes = new Mode[]{Mode.VARHANDLE};

        return new ReflectStorm.Field() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                if (parent != null)
                    throw new ReflectStormException(String.format("[Field#get] Cannot define parent to get static field '%s' on", name), modes);

                try {
                    return (T) getter.invoke();
                } catch (Throwable e) {
                    throw new ReflectStormException(String.format("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", klass.getName(), name, type.getName()), modes);
                }
            }

            @Override
            public void set(Object parent, Object value) {
                if (parent != null)
                    throw new ReflectStormException(String.format("[Field#get] Cannot define parent to get static field '%s' on", name));

                try {
                    setter.invoke(value);
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
