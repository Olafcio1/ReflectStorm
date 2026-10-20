package pl.olafcio.reflectstorm;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class ReflectStorm {
    private ReflectStorm() {}

    public static class Method {
        protected final java.lang.reflect.Method method;
        protected final Mode[] modes;

        protected Method(java.lang.reflect.Method method, Mode[] modes) {
            this.method = method;
            this.modes = modes;
        }

        @SuppressWarnings("unchecked")
        public <T> T invoke(Object parent, Object... args) throws InvocationTargetException {
            try {
                return (T) method.invoke(parent, args);
            } catch (IllegalAccessException e) {
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to access method '%s#%s(%s)' for invocation", method), e, modes);
            }
        }

        public Class getDeclaringClass() {
            return new Class(method.getDeclaringClass());
        }

        public String getName() {
            return method.getName();
        }

        public java.lang.Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        public int getParameterCount() {
            return method.getParameterCount();
        }
    }

    public static class Field {
        protected final java.lang.reflect.Field field;
        protected final Mode[] modes;

        protected Field(java.lang.reflect.Field field, Mode[] modes) {
            this.field = field;
            this.field.trySetAccessible();

            this.modes = modes;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Object parent) {
            try {
                return (T) field.get(parent);
            } catch (IllegalAccessException e) {
                if (Internal.unsafe == null)
                    throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", field), e, modes);

                try {
                    if (parent != null) {
                        return (T) Internal.unsafe.getObject(
                                parent,
                                Internal.unsafe.objectFieldOffset(field)
                        );
                    } else {
                        return (T) Internal.unsafe.getObject(
                                Internal.unsafe.staticFieldBase(field),
                                Internal.unsafe.staticFieldOffset(field)
                        );
                    }
                } catch (Throwable ex) {
                    throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-get '%s#%s[%s]'", field), ex, modes);
                }
            }
        }

        public void set(Object parent, Object value) {
            try {
                field.set(parent, value);
            } catch (IllegalAccessException e) {
                if (Internal.unsafe == null)
                    throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", field), e, modes);

                try {
                    if (parent != null) {
                        Internal.unsafe.putObject(
                                parent,
                                Internal.unsafe.objectFieldOffset(field),
                                value
                        );
                    } else {
                        Internal.unsafe.putObject(
                                Internal.unsafe.staticFieldBase(field),
                                Internal.unsafe.staticFieldOffset(field),
                                value
                        );
                    }
                } catch (Throwable ex) {
                    throw new ReflectStormException(fillout("[ReflectStorm] Failed to access field-set '%s#%s[%s]'", field), ex, modes);
                }
            }
        }

        public Class getDeclaringClass() {
            return new Class(field.getDeclaringClass());
        }

        public String getName() {
            return field.getName();
        }

        public java.lang.Class<?> getType() {
            return field.getType();
        }
    }

    public static class Class {
        protected final java.lang.Class<?> klass;
        protected final MethodHandles.Lookup lookup;

        protected Class(java.lang.Class<?> klass) {
            this.klass = klass;
            this.lookup = Internal.getLookupFor(klass);
        }

        public Method method(String name, java.lang.Class<?>... args) {
            java.lang.reflect.Method method;

            try {
                method = klass.getDeclaredMethod(name, args);
            } catch (NoSuchMethodException e) {
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to get method '%s#%s(%s)'", klass, name, args), e);
            }

            try {
                return methodHandle(method);
            } catch (IllegalAccessException e) {
                if (Internal.unsafe == null)
                    return new Method(method, new Mode[]{ Mode.REFLECTION });
                else
                    return new Method(method, new Mode[]{ Mode.REFLECTION, Mode.UNSAFE });
            }
        }

        public Field field(String name, java.lang.Class<?> type) {
            java.lang.reflect.Field field;

            try {
                field = klass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new ReflectStormException(fillout("[ReflectStorm] Failed to get field '%s#%s[%s]'", klass, name, type), e);
            }

            try {
                return fieldHandle(field);
            } catch (IllegalAccessException e) {
                if (Internal.unsafe == null)
                    return new Field(field, new Mode[]{ Mode.REFLECTION });
                else
                    return new Field(field, new Mode[]{ Mode.REFLECTION, Mode.UNSAFE });
            }
        }

        private Method methodHandle(java.lang.reflect.Method method) throws IllegalAccessException {
            var handle = lookup.unreflect(method);
            var modes  = (Internal.unsafe == null)
                            ? new Mode[]{ Mode.VARHANDLE, Mode.REFLECTION }
                            : new Mode[]{ Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE };

            return new Method(method, modes) {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T invoke(Object parent, Object... args) throws InvocationTargetException {
                    var total = new Object[args.length + 1];

                    total[0] = parent;
                    System.arraycopy(args, 0, total, 1, args.length);

                    try {
                        return (T) handle.invoke(total);
                    } catch (Throwable e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
        }

        private Field fieldHandle(java.lang.reflect.Field field) throws IllegalAccessException {
            var handle = lookup.unreflectVarHandle(field);
            var modes  = (Internal.unsafe == null)
                            ? new Mode[]{ Mode.VARHANDLE, Mode.REFLECTION }
                            : new Mode[]{ Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE };

            return new Field(field, modes) {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T get(Object parent) {
                    return (T) handle.get(parent);
                }

                @Override
                public void set(Object parent, Object value) {
                    try {
                        handle.set(parent, value);
                    } catch (UnsupportedOperationException e) {
                        super.set(parent, value);
                    }
                }
            };
        }
    }

    private static String fillout(String text, java.lang.reflect.Method method) {
        return fillout(
                text,
                method.getDeclaringClass(),
                method.getName(),
                method.getParameterTypes()
        );
    }

    private static String fillout(String text, java.lang.reflect.Field field) {
        return fillout(
                text,
                field.getDeclaringClass(),
                field.getName(),
                field.getType()
        );
    }

    private static String fillout(String text, java.lang.Class<?> klass, String name, java.lang.Class<?>... args) {
        return text.formatted(
                klass.getName(),
                name,
                Arrays.stream(args)
                      .map(java.lang.Class::getName)
                      .collect(Collectors.joining(", "))
        );
    }

    public static class Builder {
        private ClassLoader classLoader;

        public void klassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public Class klass(java.lang.Class<?> klass) {
            return new Class(klass);
        }

        public Class klass(String name) {
            try {
                if (this.classLoader != null) {
                    return new Class(this.classLoader.loadClass(name));
                } else {
                    return new Class(java.lang.Class.forName(name));
                }
            } catch (ClassNotFoundException e) {
                throw new ReflectStormException("[ReflectStorm] Failed to find class '%s'".formatted(name), e);
            }
        }
    }

    public static ReflectStorm.Builder builder() {
        return new ReflectStorm.Builder();
    }
}
