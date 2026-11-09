package pl.olafcio.reflectstorm;

import pl.olafcio.reflectstorm.impl_class.ICMethodHandle;

class ImplClass extends ICMethodHandle implements ReflectStorm.Class {
    public ImplClass(Class<?> klass) {
        super(klass);
    }
}
