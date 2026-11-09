package pl.olafcio.reflectstorm.base;

import pl.olafcio.reflectstorm.Mode;
import pl.olafcio.reflectstorm.ReflectStorm;

import java.lang.reflect.Field;

public abstract class BaseField implements ReflectStorm.Field {
    protected final Field method;
    protected final Mode[] modes;

    public BaseField(Field method, Mode[] modes) {
        this.method = method;
        this.modes = modes;
    }
}
