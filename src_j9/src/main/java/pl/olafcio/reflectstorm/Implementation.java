package pl.olafcio.reflectstorm;

import org.slf4j.LoggerFactory;

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

    @Override
    public ReflectStorm.Classpath createClasspath(ClassLoader classLoader) {
        ReflectStorm.Class bcl = ReflectStorm.builder().klass("jdk.internal.loader.BuiltinClassLoader");
        if (bcl.isExtendedBy(classLoader.getClass())) {
            final Class<?> ucp;

            try {
                ucp = Class.forName("jdk.internal.loader.URLClassPath");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("[ReflectStorm] Failed to ReflectStorm::createClasspath", e);
            }

            return new ReflectStorm.Classpath() {
                @Override
                public void addFile(String path) {
                    try {
                        addURL(new File(path).toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to Classpath#addFile", e);
                    }
                }

                @Override
                public void addURL(URL url) {
                    try {
                        Object classpath = bcl.field("ucp", ucp)
                                              .get(this.getClass().getClassLoader());

                        ReflectStorm.builder().klass(ucp)
                                .method("addURL", URL.class)
                                .invoke(classpath, url);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to Classpath#addURL", e);
                    }
                }

                @Override
                public void addURI(URI url) {
                    try {
                        addURL(url.toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to Classpath#addURI", e);
                    }
                }
            };
        }

        if (classLoader instanceof URLClassLoader) {
            ReflectStorm.Class ucl = ReflectStorm.builder().klass(URLClassLoader.class);

            return new ReflectStorm.Classpath() {
                @Override
                public void addFile(String path) {
                    try {
                        addURL(URI.create(path).toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to Classpath#addFile", e);
                    }
                }

                @Override
                public void addURL(URL url) {
                    try {
                        ucl.method("addURL", URL.class)
                           .invoke(this.getClass().getClassLoader(), url);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("[ReflectStorm] Failed to Classpath#addURL", e);
                    }
                }

                @Override
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
    public ClassLoader getClassLoader(ReflectStorm.Classpath.BuiltinCL classLoader) {
        if (classLoader == ReflectStorm.Classpath.BuiltinCL.APP) {
            try {
                return ReflectStorm.builder().klass("jdk.internal.loader.ClassLoaders")
                                             .method("appClassLoader")
                                             .invoke(null);
            } catch (InvocationTargetException e) {
                throw new UnsupportedOperationException("[ReflectStorm] getClassLoader(APP) failed", e);
            }
        } else if (classLoader == ReflectStorm.Classpath.BuiltinCL.BOOT) {
            try {
                return ReflectStorm.builder().klass("jdk.internal.loader.ClassLoaders")
                                             .method("bootLoader")
                                             .invoke(null);
            } catch (InvocationTargetException e) {
                throw new UnsupportedOperationException("[ReflectStorm] getClassLoader(BOOT) failed", e);
            }
        } else if (classLoader == ReflectStorm.Classpath.BuiltinCL.PLATFORM) {
            try {
                return ReflectStorm.builder().klass("jdk.internal.loader.ClassLoaders")
                                             .method("platformClassLoader")
                                             .invoke(null);
            } catch (InvocationTargetException e) {
                throw new UnsupportedOperationException("[ReflectStorm] getClassLoader(PLATFORM) failed", e);
            }
        }

        return null;
    }
}
