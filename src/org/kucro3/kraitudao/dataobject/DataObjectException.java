package org.kucro3.kraitudao.dataobject;

public class DataObjectException extends RuntimeException {
    public DataObjectException()
    {
    }

    public DataObjectException(String msg)
    {
        super(msg);
    }

    public DataObjectException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public static DataObjectException IncapableType(Class<?> current, Class<?> expected)
    {
        return new DataObjectException("Incapable type: " + current.getCanonicalName() + "(" + expected.getCanonicalName() + " expected)");
    }

    public static DataObjectException IncapableValue(Object value, Class<?> expected)
    {
        return new DataObjectException("Incapable value: Type of " + value.getClass().getCanonicalName() + "(" + expected.getCanonicalName() + " expected)");
    }
}
