package pl.olafcio.reflectstorm;

import org.slf4j.LoggerFactory;
import sun.misc.Launcher;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class Implementation extends Abstraction {
    @Override
    public ReflectStorm.Method createMethod(final Method method, final Mode[] modes) {
        return new ImplMethod(method, modes);
    }

    @Override
    public ReflectStorm.Field createField(final Field field, final Mode[] modes) {
        return new ImplField(field, modes);
    }

    @Override
    public ReflectStorm.Class createClass(final Class<?> klass) {
        return new ImplClass(klass);
    }

    @Override
    public void warn(String message) {
        LoggerFactory.getLogger("ReflectStorm")
                     .warn(message);
    }

    @Override
    public ReflectStorm.Classpath createClasspath(ClassLoader classLoader) throws UnsupportedOperationException {
        if (classLoader instanceof URLClassLoader) {
            final ReflectStorm.Class ucl = ReflectStorm.builder().klass(URLClassLoader.class);

            return new ReflectStorm.Classpath() {
                public void addFile(String path) {
                    try {
                        addURL(new File(path).toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to convert string path to URL in Classpath#addFile", e);
                    }
                }

                public void addURL(URL url) {
                    try {
                        ucl.method("addURL", URL.class)
                           .invoke(this.getClass().getClassLoader(), url);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to Classpath#addURL", e);
                    }
                }

                public void addURI(URI url) {
                    try {
                        addURL(url.toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to convert URI to URL in Classpath#addURI", e);
                    }
                }
            };
        }

        throw new UnsupportedOperationException(String.format("[ReflectStorm] Unable to modify classpath of %s; presumably a non-JDK classloader", classLoader));
    }

    @Override
    public ClassLoader getClassLoader(ReflectStorm.Classpath.BuiltinCL classLoader) throws UnsupportedOperationException {
        if (classLoader == ReflectStorm.Classpath.BuiltinCL.APP)
            return Launcher.getLauncher().getClassLoader();
        else
            throw new UnsupportedOperationException(String.format("[ReflectStorm] Unable to get classloader '%s' as it doesn't exist in this JVM version", classLoader.name()));
    }
}
