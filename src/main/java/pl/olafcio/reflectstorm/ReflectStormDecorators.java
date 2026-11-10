package pl.olafcio.reflectstorm;

import java.lang.reflect.InvocationTargetException;

public final class ReflectStormDecorators {
    private ReflectStormDecorators() {}

    public abstract static class Method implements ReflectStorm.Method {
        private final ReflectStorm.Method delegate;

        protected Method(ReflectStorm.Method delegate) {
            this.delegate = delegate;
        }

        public <T> T invoke(Object parent, Object... args) throws InvocationTargetException {
            return delegate.<T>invoke(parent, args);
        }

        public ReflectStorm.Class getDeclaringClass() {
            return delegate.getDeclaringClass();
        }

        public String getName() {
            return delegate.getName();
        }

        public java.lang.Class<?>[] getParameterTypes() {
            return delegate.getParameterTypes();
        }

        public int getParameterCount() {
            return delegate.getParameterCount();
        }
    }

    public abstract static class Field implements ReflectStorm.Field {
        private final ReflectStorm.Field delegate;

        protected Field(ReflectStorm.Field delegate) {
            this.delegate = delegate;
        }

        public <T> T get(Object parent) {
            return delegate.<T>get(parent);
        }

        public void set(Object parent, Object value) {
            delegate.set(parent, value);
        }

        public ReflectStorm.Class getDeclaringClass() {
            return delegate.getDeclaringClass();
        }

        public String getName() {
            return delegate.getName();
        }

        public java.lang.Class<?> getType() {
            return delegate.getType();
        }
    }

    public abstract static class Class implements ReflectStorm.Class {
        private final ReflectStorm.Class delegate;

        protected Class(ReflectStorm.Class delegate) {
            this.delegate = delegate;
        }

        public ReflectStorm.Method method(String name, java.lang.Class<?>... args) {
            return delegate.method(name, args);
        }

        public ReflectStorm.Field field(String name, java.lang.Class<?> type) {
            return delegate.field(name, type);
        }

        public boolean isExtendedBy(java.lang.Class<?> klass) {
            return delegate.isExtendedBy(klass);
        }

        public boolean isExtendedBy(String klassName) {
            return delegate.isExtendedBy(klassName);
        }
    }
}
