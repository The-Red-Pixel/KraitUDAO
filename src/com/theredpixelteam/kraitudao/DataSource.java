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

    public default Transition commit(Collection<Object> objects) throws DataSourceException
    {
        return commit(null, objects);
    }

    public Transition commit(Transition transition, Collection<Object> objects) throws DataSourceException;

    public default <T> Transition commit(T object, Class<T> type) throws DataSourceException
    {
        return commit(null, object, type);
    }

    public <T> Transition commit(Transition transition, T object, Class<T> type) throws DataSourceException;

    public <T> Transition remove(T object) throws DataSourceException;

    public <T> Transition remove(Transition transition, T object) throws DataSourceException;

    public <T> Transition clear() throws DataSourceException;

    public <T> Transition clear(Transition transition) throws DataSourceException;

    public <T> Transition removeVaguely(T object) throws DataSourceException;

    public <T> Transition removeVaguely(Transition transition, T object) throws DataSourceException;
}
