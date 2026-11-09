package pl.olafcio.reflectstorm;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class ImplClass implements ReflectStorm.Class {
    protected final MethodHandles.Lookup lookup;
    private final Class<?> klass;

    public ImplClass(Class<?> klass) {
        this.klass = klass;
        this.lookup = MethodHandleAccess.getLookupFor(klass);
    }

    private ReflectStorm.Field fieldTotalHandleStatic(final String name, final Class<?> type) throws IllegalAccessException, NoSuchFieldException {
        final var handle = lookup.findStaticVarHandle(klass, name, type);

        return new ReflectStorm.Field() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                if (parent != null)
                    throw new ReflectStormException("[Field#get] Cannot define parent to get static field '%s' on".formatted(name));

                return (T) handle.get();
            }

            @Override
            public void set(Object parent, Object value) {
                if (parent != null)
                    throw new ReflectStormException("[Field#get] Cannot define parent to get static field '%s' on".formatted(name));

                handle.set(value);
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

    private ReflectStorm.Field fieldTotalHandle(final String name, final Class<?> type) throws IllegalAccessException, NoSuchFieldException {
        var handle = lookup.findVarHandle(klass, name, type);

        return new ReflectStorm.Field() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                return (T) handle.get(parent);
            }

            @Override
            public void set(Object parent, Object value) {
                handle.set(parent, value);
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

    private ReflectStorm.Field fieldHandle(Field field) throws IllegalAccessException, NoSuchFieldException {
        var getter = lookup.unreflectGetter(field);
        var setter = (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC
                        ? lookup.findStaticSetter(field.getDeclaringClass(), field.getName(), field.getType())
                        : lookup.findSetter(field.getDeclaringClass(), field.getName(), field.getType());

        var modes = (UnsafeAccess.unsafe == null)
                ? new Mode[]{Mode.VARHANDLE, Mode.REFLECTION}
                : new Mode[]{Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE};

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
                    super.set(parent, value);
                }
            }
        };
    }

    private ReflectStorm.Method methodHandle(Method method) throws IllegalAccessException {
        var handle = lookup.unreflect(method);
        var modes = (UnsafeAccess.unsafe == null)
                ? new Mode[]{Mode.VARHANDLE, Mode.REFLECTION}
                : new Mode[]{Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE};

        return new ReflectStormDecorators.Method(Abstraction.get().createMethod(method, modes)) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T invoke(Object parent, Object... args) throws InvocationTargetException {
                Object[] total;

                if (parent != null) {
                    total = new Object[args.length + 1];
                    total[0] = parent;

                    System.arraycopy(args, 0, total, 1, args.length);
                } else {
                    total = args;
                }

                try {
                    var handle1 = handle.asType(handle.type().changeReturnType(Object.class));
                    var handle2 = MethodHandles.spreadInvoker(handle1.type(), 0);

                    return (T) handle2.invokeExact(handle, total);
                } catch (Throwable e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
    }

    @Override
    public ReflectStorm.Field field(String name, Class<?> type) {
        Field field;

        try {
            field = klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            try {
                return fieldTotalHandle(name, type);
            } catch (Throwable ex) {
                try {
                    return fieldTotalHandleStatic(name, type);
                } catch (Throwable exc) {
                    throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to get field '%s#%s[%s]'", klass, name, type), exc);
                }
            }
        }

        try {
            return fieldHandle(field);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            if (UnsafeAccess.unsafe == null)
                return Abstraction.get().createField(field, new Mode[]{Mode.REFLECTION});
            else
                return Abstraction.get().createField(field, new Mode[]{Mode.REFLECTION, Mode.UNSAFE});
        }
    }

    @Override
    public ReflectStorm.Method method(String name, Class<?>... args) {
        Method method;

        try {
            method = klass.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to get method '%s#%s(%s)'", klass, name, args), e);
        }

        try {
            return methodHandle(method);
        } catch (IllegalAccessException e) {
            if (UnsafeAccess.unsafe == null)
                return Abstraction.get().createMethod(method, new Mode[]{Mode.REFLECTION});
            else
                return Abstraction.get().createMethod(method, new Mode[]{Mode.REFLECTION, Mode.UNSAFE});
        }
    }
}
