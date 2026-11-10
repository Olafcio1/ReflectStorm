package pl.olafcio.reflectstorm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class ImplClass implements ReflectStorm.Class {
    private final Class<?> klass;

    public ImplClass(Class<?> klass) {
        this.klass = klass;
    }

    @Override
    public ReflectStorm.Field field(String name, Class<?> type) {
        Field field;

        try {
            field = klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to get field '%s#%s[%s]'", klass, name, type), e);
        }

        if (UnsafeAccess.unsafe == null)
            return Abstraction.get().createField(field, new Mode[]{Mode.REFLECTION});
        else
            return Abstraction.get().createField(field, new Mode[]{Mode.REFLECTION, Mode.UNSAFE});
    }

    @Override
    public ReflectStorm.Method method(String name, Class<?>... args) {
        Method method;

        try {
            method = klass.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new ReflectStormException(Util.fillout("[ReflectStorm] Failed to get method '%s#%s(%s)'", klass, name, args), e);
        }

        if (UnsafeAccess.unsafe == null)
            return Abstraction.get().createMethod(method, new Mode[]{Mode.REFLECTION});
        else
            return Abstraction.get().createMethod(method, new Mode[]{Mode.REFLECTION, Mode.UNSAFE});
    }

    @Override
    public boolean isExtendedBy(String klassName) {
        try {
            return klass.isAssignableFrom(Class.forName(klassName));
        } catch (ClassNotFoundException e) {
            throw new ReflectStormException(String.format("[ReflectStorm] Failed to find class '%s'", klassName), e);
        }
    }

    @Override
    public boolean isExtendedBy(Class<?> klass) {
        return klass.isAssignableFrom(klass);
    }
}
