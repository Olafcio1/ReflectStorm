package pl.olafcio.reflectstorm;

import pl.olafcio.reflectstorm.impl_class.ICMethodHandle;

class ImplClass extends ICMethodHandle implements ReflectStorm.Class {
    public ImplClass(Class<?> klass) {
        super(klass);
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
