package pl.olafcio.reflectstorm.base;

import pl.olafcio.reflectstorm.ReflectStorm;

public abstract class BaseClass implements ReflectStorm.Class {
    protected final Class<?> klass;

    public BaseClass(Class<?> klass) {
        this.klass = klass;
    }
}
