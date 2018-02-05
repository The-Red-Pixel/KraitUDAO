package org.kucro3.kraitudao.dataobject;

public interface ValueObject {
    public String getName();

    public Class<?> getType();

    public Object get(Object object);

    public <T> T get(Object object, Class<T> type);

    public void set(Object object, Object value);

    public <T> void set(Object object, T value, Class<T> type);

    public Class<?> getOwnerType();

    public DataObject getOwner();

    public default boolean isKey()
    {
        return isPrimaryKey() || isSecondaryKey();
    }

    public boolean isPrimaryKey();

    public boolean isSecondaryKey();
}
