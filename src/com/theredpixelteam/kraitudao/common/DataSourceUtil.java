package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.DataSource;
import com.theredpixelteam.kraitudao.DataSourceException;
import com.theredpixelteam.kraitudao.Transition;

import java.util.Collection;

public final class DataSourceUtil {
    private DataSourceUtil()
    {
    }

    public static DataSource synchronizedDataSource(DataSource dataSource)
    {
        return new SynchronizedDataSource(dataSource);
    }

    private static final class SynchronizedDataSource implements DataSource
    {
        private SynchronizedDataSource(DataSource object)
        {
            this.object = object;
        }

        @Override
        public synchronized <T> void pull(T object, Class<T> type) throws DataSourceException
        {
            this.object.pull(object, type);
        }

        @Override
        public synchronized <T> Collection<T> pull(Class<T> type) throws DataSourceException
        {
            return this.object.pull(type);
        }

        @Override
        public synchronized <T> Collection<T> pullVaguely(T object) throws DataSourceException
        {
            return this.object.pullVaguely(object);
        }

        @Override
        public synchronized Transition commit(Transition transition, Collection<Object> objects) throws DataSourceException
        {
            return this.object.commit(transition, objects);
        }

        @Override
        public synchronized <T> Transition commit(Transition transition, T object, Class<T> type) throws DataSourceException
        {
            return this.object.commit(transition, object, type);
        }

        @Override
        public synchronized <T> Transition remove(T object) throws DataSourceException
        {
            return this.object.remove(object);
        }

        @Override
        public synchronized <T> Transition remove(Transition transition, T object) throws DataSourceException
        {
            return this.object.remove(transition, object);
        }

        @Override
        public synchronized <T> Transition clear() throws DataSourceException
        {
            return this.object.clear();
        }

        @Override
        public synchronized <T> Transition clear(Transition transition) throws DataSourceException
        {
            return this.object.clear(transition);
        }

        @Override
        public synchronized <T> Transition removeVaguely(T object) throws DataSourceException
        {
            return this.object.removeVaguely(object);
        }

        @Override
        public synchronized <T> Transition removeVaguely(Transition transition, T object) throws DataSourceException
        {
            return this.object.removeVaguely(transition, object);
        }

        private final DataSource object;
    }
}
