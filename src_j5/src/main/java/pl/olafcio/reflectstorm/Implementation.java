package pl.olafcio.reflectstorm;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Implementation extends Abstraction {
    @Override
    public ReflectStorm.Method createMethod(final Method method, final Mode[] modes) {
        return new pl.olafcio.reflectstorm.ImplMethod(method, modes);
    }

    @Override
    public ReflectStorm.Field createField(final Field field, final Mode[] modes) {
        try {
            field.setAccessible(true);
        } catch (Throwable ignored) {
        }

        return new ImplField(field, modes);
    }

    @Override
    public ReflectStorm.Class createClass(final Class<?> klass) {
        return new ImplClass(klass);
    }

    @Override
    public void warn(String message) {
        Logger.getLogger("ReflectStorm")
              .warn(message);
    }
}
