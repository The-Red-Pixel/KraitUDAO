package com.theredpixelteam.kraitudao;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface DataSource {
    public default void pull(Object object) throws DataSourceException
    {
        pull(object, (Class) object.getClass());
    }

    public <T> void pull(T object, Class<T> type) throws DataSourceException;

    public <T> Collection<T> pull(Class<T> type) throws DataSourceException;

    public <T> Collection<T> pullVaguely(T object) throws DataSourceException;

    public default Transaction commit(Collection<Object> objects) throws DataSourceException
    {
        return commit(null, objects);
    }

    public Transaction commit(Transaction transition, Collection<Object> objects) throws DataSourceException;

    public default <T> Transaction commit(T object, Class<T> type) throws DataSourceException
    {
        return commit(null, object, type);
    }

    public <T> Transaction commit(Transaction transition, T object, Class<T> type) throws DataSourceException;

    public default <T> Transaction remove(T object) throws DataSourceException
    {
        return remove(null, object);
    }

    public <T> Transaction remove(Transaction transition, T object) throws DataSourceException;

    public default <T> Transaction clear() throws DataSourceException
    {
        return clear(null);
    }

    public <T> Transaction clear(Transaction transition) throws DataSourceException;

    public default <T> Transaction removeVaguely(T object) throws DataSourceException
    {
        return removeVaguely(null, object);
    }

    public <T> Transaction removeVaguely(Transaction transition, T object) throws DataSourceException;
}
