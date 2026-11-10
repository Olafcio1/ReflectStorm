package pl.olafcio.reflectstorm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p><h2>ReflectStorm: A cross-compatible Java reflection wrapper.</h2>
 * <hr/>
 * <p>ReflectStorm utilizes the power of <i>{@linkplain Object#getClass() reflection}</i>,
 *                                       <i>{@linkplain java.lang.invoke.MethodHandles method handles}</i> and
 *                                       <i>{@linkplain sun.misc.Unsafe unsafe}</i>.
 * <p><u>Thanks to them, you can:</u>
 * <ul>
 *     <li>reflect classes: {@link ReflectStorm#builder() ReflectStorm.builder()},</li>
 *     <li>reflect methods: {@link ReflectStorm.Method},</li>
 *     <li>reflect fields: {@link ReflectStorm.Field};</li>
 * </ul>
 * <p>and more coming soon!
 */
public final class ReflectStorm {
    private ReflectStorm() {}

    /**
     * A wrapped method.
     * <br><br>
     * You can get this object via
     * <ol>
     *     <li>{@link Class#method(String, java.lang.Class[]) Class#method(...)} (for class objects),</li>
     *     <li>{@link Abstraction#createMethod Abstraction.get().createMethod(...)} (for {@linkplain java.lang.reflect.Method java reflection methods}).</li>
     * </ol>
     */
    public interface Method {
        /**
         * Invokes the method.
         * @param parent The object to invoke on. {@code null} if static.
         * @param args The parameters to specify.
         * @return The return value of the invoked method.
         * @param <T> The return type.
         * @throws InvocationTargetException Thrown when the invoked method throws an exception.
         */
        <T> T invoke(Object parent, Object... args) throws InvocationTargetException;

        /**
         * Returns the class which the method was declared in.
         * @return The class which the method was declared in.
         */
        Class getDeclaringClass();

        /**
         * Returns the name of the method.
         */
        String getName();

        /**
         * Returns the method's parameter types.
         * @return The method's parameter types.
         */
        java.lang.Class<?>[] getParameterTypes();

        /**
         * Returns the method's parameter count.
         * @return How many parameters does this method have.
         */
        int getParameterCount();
    }

    /**
     * A wrapped field.
     */
    public interface Field {
        /**
         * Returns the field's value on the given object.
         * @param parent The object to retrieve the field's value from. {@code null} if static.
         * @return The field's value.
         * @param <T> Type to cast the field's value to.
         */
        <T> T get(Object parent);

        /**
         * Sets the field's value on the given object.
         * @param parent The object to set the field's value on. {@code null} if static.
         * @param value The field's new value.
         */
        void set(Object parent, Object value);

        /**
         * Returns the class which the field was declared in.
         * @return The class which the field was declared in.
         */
        Class getDeclaringClass();

        /**
         * Returns the name of the field.
         */
        String getName();

        /**
         * Returns the field's type.
         */
        java.lang.Class<?> getType();
    }

    /**
     * A wrapped class.
     */
    public interface Class {
        /**
         * Finds a method with the given name and parameters.
         * @param name The name of the method to find.
         * @param args The parameter types of the method to find.
         * @return The found method, wrapped with {@link Method}.
         */
        Method method(String name, java.lang.Class<?>... args);

        /**
         * Finds a field with the given name and type.
         * @param name The name of the field to find.
         * @param type The type of the field to find.
         * @return The found field, wrapped with {@link Field}.
         */
        Field field(String name, java.lang.Class<?> type);

        /**
         * Returns whether the given class extends this class.
         * @param klass The class to check.
         * @return Whether the given class extends this class.
         */
        boolean isExtendedBy(java.lang.Class<?> klass);

        /**
         * Returns whether a class under the given name extends this class.
         * @param klassName The name of the class to check.
         * @return Whether the given class extends this class.
         */
        boolean isExtendedBy(String klassName);
    }

    public static class Builder {
        private ClassLoader classLoader;

        public void klassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public Class klass(java.lang.Class<?> klass) {
            return Abstraction.get().createClass(klass);
        }

        public Class klass(String name) {
            try {
                if (this.classLoader != null) {
                    return Abstraction.get().createClass(this.classLoader.loadClass(name));
                } else {
                    return Abstraction.get().createClass(java.lang.Class.forName(name));
                }
            } catch (ClassNotFoundException e) {
                throw new ReflectStormException(String.format("[ReflectStorm] Failed to find class '%s'", name), e);
            }
        }
    }

    /**
     * Returns a builder to load a class.
     */
    public static ReflectStorm.Builder builder() {
        return new ReflectStorm.Builder();
    }
}
