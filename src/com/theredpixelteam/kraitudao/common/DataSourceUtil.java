package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.DataSource;
import com.theredpixelteam.kraitudao.DataSourceException;
import com.theredpixelteam.kraitudao.Transaction;

import java.awt.font.TransformAttribute;
import java.util.Collection;
import java.util.Optional;

/**
 * 版权所有（C） The Red Pixel <theredpixelteam.com>
 * 版权所有（C)  KuCrO3 Studio
 * 这一程序是自由软件，你可以遵照自由软件基金会出版的GNU通用公共许可证条款
 * 来修改和重新发布这一程序。或者用许可证的第二版，或者（根据你的选择）用任
 * 何更新的版本。
 * 发布这一程序的目的是希望它有用，但没有任何担保。甚至没有适合特定目的的隐
 * 含的担保。更详细的情况请参阅GNU通用公共许可证。
 */
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
        public synchronized Transaction commit(Collection<Object> objects) throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.commit(objects));
        }

        @Override
        public synchronized Transaction commit(Transaction transition, Collection<Object> objects) throws DataSourceException
        {
            return this.object.commit(transition, objects);
        }

        @Override
        public synchronized <T> Transaction commit(T object, Class<T> type) throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.commit(object, type));
        }

        @Override
        public synchronized <T> Transaction commit(Transaction transition, T object, Class<T> type) throws DataSourceException
        {
            return this.object.commit(transition, object, type);
        }

        @Override
        public synchronized <T> Transaction remove(T object) throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.remove(object));
        }

        @Override
        public synchronized <T> Transaction remove(Transaction transition, T object) throws DataSourceException
        {
            return this.object.remove(transition, object);
        }

        @Override
        public synchronized <T> Transaction clear() throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.clear());
        }

        @Override
        public synchronized <T> Transaction clear(Transaction transition) throws DataSourceException
        {
            return this.object.clear(transition);
        }

        @Override
        public synchronized <T> Transaction removeVaguely(T object) throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.removeVaguely(object));
        }

        @Override
        public synchronized <T> Transaction removeVaguely(Transaction transition, T object) throws DataSourceException
        {
            return this.object.removeVaguely(transition, object);
        }

        @Override
        public void waitForTransaction()
        {
            this.object.waitForTransaction();
        }

        private final DataSource object;

        private class SynchronizedTransaction implements Transaction
        {
            private SynchronizedTransaction(Transaction transaction)
            {
                this.transaction = transaction;
            }

            @Override
            public boolean push() throws DataSourceException
            {
                synchronized (SynchronizedDataSource.this) {
                    return this.transaction.push();
                }
            }

            @Override
            public boolean cancel()
            {
                synchronized (SynchronizedDataSource.this) {
                    return this.cancel();
                }
            }

            @Override
            public boolean equals(Object object)
            {
                return this.transaction.equals(object);
            }

            @Override
            public Optional<Exception> getLastException()
            {
                synchronized (SynchronizedDataSource.this) {
                    return this.transaction.getLastException();
                }
            }

            private final Transaction transaction;
        }
    }
}
