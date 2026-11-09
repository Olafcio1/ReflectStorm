package pl.olafcio.reflectstorm;

import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Implementation extends Abstraction {
    @Override
    public ReflectStorm.Method createMethod(Method method, Mode[] modes) {
        return new ImplMethod(method, modes);
    }

    @Override
    public ReflectStorm.Field createField(Field field, Mode[] modes) {
        return new ImplField(field, modes);
    }

    @Override
    public ReflectStorm.Class createClass(Class<?> klass) {
        return new ImplClass(klass);
    }

    @Override
    public void warn(String message) {
        LoggerFactory.getLogger("ReflectStorm")
                     .warn(message);
    }
}
