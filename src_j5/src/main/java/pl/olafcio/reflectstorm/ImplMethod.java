package pl.olafcio.reflectstorm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static pl.olafcio.reflectstorm.Util.fillout;

class ImplMethod implements ReflectStorm.Method {
    private final Method method;
    private final Mode[] modes;

    public ImplMethod(Method method, Mode[] modes) {
        this.method = method;
        this.modes = modes;
    }

    public int getParameterCount() {
        return method.getParameterTypes().length;
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public String getName() {
        return method.getName();
    }

    public ReflectStorm.Class getDeclaringClass() {
        return Abstraction.get().createClass(method.getDeclaringClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Object parent, Object... args) throws InvocationTargetException {
        try {
            return (T) method.invoke(parent, args);
        } catch (IllegalAccessException e) {
            throw new ReflectStormException(fillout("[ReflectStorm] Failed to access method '%s#%s(%s)' for invocation", method), e, modes);
        }
    }
}
