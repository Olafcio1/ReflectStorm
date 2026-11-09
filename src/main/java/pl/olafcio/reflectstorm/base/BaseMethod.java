package pl.olafcio.reflectstorm.base;

import pl.olafcio.reflectstorm.Mode;
import pl.olafcio.reflectstorm.ReflectStorm;

import java.lang.reflect.Method;

public abstract class BaseMethod implements ReflectStorm.Method {
    protected final Method method;
    protected final Mode[] modes;

    public BaseMethod(Method method, Mode[] modes) {
        this.method = method;
        this.modes = modes;
    }
}
