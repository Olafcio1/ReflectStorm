package pl.olafcio.reflectstorm;

public enum Util {
    ;

    public static String fillout(String text, Class<?> klass, String name, Class<?>... args) {
        StringBuilder argsString = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            argsString.append(args[i].getName());

            if (i != args.length - 1)
                argsString.append(", ");
        }

        return String.format(text, klass.getName(), name, argsString);
    }

    public static String fillout(String text, java.lang.reflect.Method method) {
        return fillout(
                text,
                method.getDeclaringClass(),
                method.getName(),
                method.getParameterTypes()
        );
    }

    public static String fillout(String text, java.lang.reflect.Field field) {
        return fillout(
                text,
                field.getDeclaringClass(),
                field.getName(),
                field.getType()
        );
    }
}
