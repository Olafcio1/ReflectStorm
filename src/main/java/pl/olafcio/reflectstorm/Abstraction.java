package pl.olafcio.reflectstorm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Abstraction {
    private static final Abstraction INSTANCE;

    static {
        try {
            INSTANCE = (Abstraction) Class.forName("pl.olafcio.reflectstorm.Implementation")
                                          .getDeclaredConstructor()
                                          .newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to create Java-version based implementation", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to create Java-version based implementation", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to create Java-version based implementation", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to create Java-version based implementation", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to create Java-version based implementation", e);
        }
    }

    public static Abstraction get() {
        return INSTANCE;
    }

    public abstract ReflectStorm.Method createMethod(Method method, Mode[] modes);
    public abstract ReflectStorm.Field createField(Field field, Mode[] modes);
    public abstract ReflectStorm.Class createClass(Class<?> klass);

    public abstract void warn(String message);

    public abstract ReflectStorm.Classpath createClasspath(ClassLoader classLoader) throws UnsupportedOperationException;
    public abstract ClassLoader getClassLoader(ReflectStorm.Classpath.BuiltinCL classLoader) throws UnsupportedOperationException;
}
