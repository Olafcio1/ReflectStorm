package pl.olafcio.reflectstorm;

import java.lang.reflect.Field;

public enum Shared {
    ;

    public static void setAny(Field field, Object parent, Object value) throws IllegalAccessException {
        if (field.getType() == int.class)
            field.setInt(parent, (Integer) value);
        else if (field.getType() == short.class)
            field.setShort(parent, (Short) value);
        else if (field.getType() == long.class)
            field.setLong(parent, (Long) value);
        else if (field.getType() == double.class)
            field.setDouble(parent, (Double) value);
        else if (field.getType() == byte.class)
            field.setByte(parent, (Byte) value);
        else if (field.getType() == float.class)
            field.setFloat(parent, (Float) value);
        else if (field.getType() == char.class)
            field.setChar(parent, (Character) value);
        else if (field.getType() == boolean.class)
            field.setBoolean(parent, (Boolean) value);
        else
            field.set(parent, value);
    }
}
