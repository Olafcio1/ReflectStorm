package pl.olafcio.reflectstorm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ImplClass implements ReflectStorm.Class {
    protected final MethodHandles.Lookup lookup;
    private final Class<?> klass;

    public ImplClass(Class<?> klass) {
        this.klass = klass;
        this.lookup = MethodHandleAccess.getLookupFor(klass);
    }

    private ReflectStorm.Field fieldTotalHandleStatic(final String name, final Class<?> type) throws IllegalAccessException, NoSuchFieldException {
        VarHandle handle = lookup.findStaticVarHandle(klass, name, type);

        return new ReflectStorm.Field() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                if (parent != null)
                    throw new ReflectStormException(String.format("[Field#get] Cannot define parent to get static field '%s' on", name));

                return (T) handle.get();
            }

            @Override
            public void set(Object parent, Object value) {
                if (parent != null)
                    throw new ReflectStormException(String.format("[Field#get] Cannot define parent to get static field '%s' on", name));

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
        VarHandle handle = lookup.findVarHandle(klass, name, type);

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

    private ReflectStorm.Field fieldHandle(Field field) throws IllegalAccessException {
        VarHandle handle = lookup.unreflectVarHandle(field);
        Mode[] modes = (UnsafeAccess.unsafe == null)
                ? new Mode[]{Mode.VARHANDLE, Mode.REFLECTION}
                : new Mode[]{Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE};

        return new ReflectStormDecorators.Field(Abstraction.get().createField(field, modes)) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Object parent) {
                if (parent == null)
                    return (T) handle.get();
                else
                    return (T) handle.get(parent);
            }

            @Override
            public void set(Object parent, Object value) {
                try {
                    if (parent == null)
                        handle.set(value);
                    else
                        handle.set(parent, value);
                } catch (UnsupportedOperationException e) {
                    super.set(parent, value);
                }
            }
        };
    }

    private ReflectStorm.Method methodHandle(Method method) throws IllegalAccessException {
        MethodHandle handle = lookup.unreflect(method);
        Mode[] modes = (UnsafeAccess.unsafe == null)
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
                    MethodHandle handle1 = handle.asType(handle.type().changeReturnType(Object.class));
                    MethodHandle handle2 = MethodHandles.spreadInvoker(handle1.type(), 0);

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
        } catch (IllegalAccessException e) {
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
