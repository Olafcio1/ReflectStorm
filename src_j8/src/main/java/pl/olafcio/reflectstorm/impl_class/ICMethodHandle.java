package pl.olafcio.reflectstorm.impl_class;

import pl.olafcio.reflectstorm.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ICMethodHandle extends ICFieldHandle {
    public ICMethodHandle(Class<?> klass) {
        super(klass);
    }

    private ReflectStorm.Method methodHandle(final Method method) throws IllegalAccessException {
        final MethodHandle handle = lookup.unreflect(method);
        final Mode[] modes = (UnsafeAccess.unsafe == null)
                ? new Mode[]{Mode.VARHANDLE, Mode.REFLECTION}
                : new Mode[]{Mode.VARHANDLE, Mode.REFLECTION, Mode.UNSAFE};

        return new ReflectStormDecorators.Method(Abstraction.get().createMethod(method, modes)) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T invoke(Object parent, Object... args) throws InvocationTargetException {
                Object[] total = new Object[args.length + 1];

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
