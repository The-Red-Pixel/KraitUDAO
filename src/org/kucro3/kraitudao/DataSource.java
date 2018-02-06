package org.kucro3.kraitudao;

@SuppressWarnings("unchecked")
public interface DataSource {
    public default void query(Object object) throws DataSourceException
    {
        query(object, (Class) object.getClass());
    }

    public <T> void query(T object, Class<T> type) throws DataSourceException;

    public default void commit(Object object) throws DataSourceException
    {
        commit(object, (Class) object.getClass());
    }

    public <T> void commit(T object, Class<T> type) throws DataSourceException;
}
