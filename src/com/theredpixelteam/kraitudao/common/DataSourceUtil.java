/*
 * DataSourceUtil.java
 *
 * Copyright (C) 2018 The Red Pixel <theredpixelteam.com>
 * Copyright (C) 2018 KuCrO3 Studio <kucro3.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.DataSource;
import com.theredpixelteam.kraitudao.DataSourceException;
import com.theredpixelteam.kraitudao.Transaction;
import com.theredpixelteam.redtea.function.SupplierWithThrowable;
import com.theredpixelteam.redtea.util.Optional;

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
        public synchronized <T> void pull(T object) throws DataSourceException
        {
            this.object.pull(object);
        }

        @Override
        public synchronized <T> boolean pull(T object, Class<T> type) throws DataSourceException
        {
            return this.object.pull(object, type);
        }

        @Override
        public synchronized <T> Collection<T> pull(Class<T> type) throws DataSourceException
        {
            return this.object.pull(type);
        }

        @Override
        public synchronized <T, X extends Throwable> Collection<T> pull(Class<T> type, SupplierWithThrowable<T, X> constructor)
                throws DataSourceException
        {
            return this.object.pull(type, constructor);
        }

        @Override
        public <T, X extends Throwable> T pull(Class<T> type, SupplierWithThrowable<T, X> constructor, Class<?>... signatures)
                throws DataSourceException
        {
            return this.object.pull(type, constructor, signatures);
        }

        @Override
        public <T> T pull(Class<T> type, T object, Class<?>... signatures) throws DataSourceException
        {
            return this.object.pull(type, object, signatures);
        }

        @Override
        public <T> T pull(Class<T> type, Class<?>... signatures) throws DataSourceException
        {
            return this.object.pull(type, signatures);
        }

        @Override
        public synchronized <T> Collection<T> pullVaguely(T object)
                throws DataSourceException
        {
            return this.object.pullVaguely(object);
        }

        @Override
        public synchronized <T> Collection<T> pullVaguely(T object, Class<T> type) throws DataSourceException
        {
            return this.object.pullVaguely(object, type);
        }

        @Override
        public synchronized <T, X extends Throwable> Collection<T> pullVaguely(T object, Class<T> type, SupplierWithThrowable<T, X> constructor)
                throws DataSourceException
        {
            return this.object.pullVaguely(object, type, constructor);
        }

        @Override
        public synchronized <T> Transaction commit(T object) throws DataSourceException
        {
            return this.object.commit(object);
        }

        @Override
        public synchronized <T> void commitInstantly(T object) throws DataSourceException
        {
            this.object.commitInstantly(object);
        }

        @Override
        public synchronized <T> Transaction commit(Transaction transaction, T object) throws DataSourceException
        {
            return this.object.commit(transaction, object);
        }

        @Override
        public synchronized <T> void commitInstantly(Transaction transaction, T object) throws DataSourceException
        {
            this.object.commitInstantly(transaction, object);
        }

        @Override
        public synchronized <T> Transaction commit(T object, Class<T> type)
                throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.commit(object, type));
        }

        @Override
        public synchronized <T> void commitInstantly(T object, Class<T> type) throws DataSourceException
        {
            this.object.commitInstantly(object, type);
        }

        @Override
        public <T> Transaction commit(T object, Class<T> type, Class<?>... signatures) throws DataSourceException
        {
            return this.object.commit(object, type, signatures);
        }

        @Override
        public <T> void commitInstantly(T object, Class<T> type, Class<?>... signatures) throws DataSourceException
        {
            this.object.commitInstantly(object, type, signatures);
        }

        @Override
        public synchronized <T> Transaction commit(Transaction transition, T object, Class<T> type)
                throws DataSourceException
        {
            return this.object.commit(transition, object, type);
        }

        @Override
        public synchronized <T> void commitInstantly(Transaction transaction, T object, Class<T> type) throws DataSourceException
        {
            this.object.commitInstantly(transaction, object, type);
        }

        @Override
        public <T> Transaction commit(Transaction transaction, T object, Class<T> type, Class<?>... signatures)
                throws DataSourceException
        {
            return this.object.commit(transaction, object, type, signatures);
        }

        @Override
        public <T> void commitInstantly(Transaction transaction, T object, Class<T> type, Class<?>... signatures)
                throws DataSourceException
        {
            this.object.commitInstantly(transaction, object, type, signatures);
        }

        @Override
        public synchronized <T> Transaction remove(T object)
                throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.remove(object));
        }

        @Override
        public synchronized <T> void removeInstantly(T object) throws DataSourceException
        {
            this.object.removeInstantly(object);
        }

        @Override
        public synchronized <T> Transaction remove(T object, Class<T> type) throws DataSourceException
        {
            return this.object.remove(object, type);
        }

        @Override
        public synchronized <T> void removeInstantly(T object, Class<T> type) throws DataSourceException
        {
            this.object.removeInstantly(object, type);
        }

        @Override
        public synchronized <T> Transaction remove(Transaction transition, T object)
                throws DataSourceException
        {
            return this.object.remove(transition, object);
        }

        @Override
        public synchronized <T> void removeInstantly(Transaction transaction, T object) throws DataSourceException
        {
            this.object.removeInstantly(transaction, object);
        }

        @Override
        public synchronized <T> Transaction remove(Transaction transaction, T object, Class<T> type)
                throws DataSourceException
        {
            return this.object.remove(transaction, object, type);
        }

        @Override
        public synchronized <T> void removeInstantly(Transaction transaction, T object, Class<T> type) throws DataSourceException
        {
            this.object.removeInstantly(transaction, object, type);
        }

        @Override
        public synchronized Transaction clear() throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.clear());
        }

        @Override
        public synchronized void clearInstantly() throws DataSourceException
        {
            this.object.clearInstantly();
        }

        @Override
        public synchronized Transaction clear(Transaction transition) throws DataSourceException
        {
            return this.object.clear(transition);
        }

        @Override
        public synchronized void clearInstantly(Transaction transaction) throws DataSourceException
        {
            this.object.clearInstantly(transaction);
        }

        @Override
        public synchronized <T> Transaction removeVaguely(T object)
                throws DataSourceException
        {
            return new SynchronizedTransaction(this.object.removeVaguely(object));
        }

        @Override
        public synchronized <T> Transaction removeVaguely(T object, Class<T> type) throws DataSourceException
        {
            return this.object.removeVaguely(object, type);
        }

        @Override
        public synchronized <T> Transaction removeVaguely(Transaction transition, T object)
                throws DataSourceException
        {
            return this.object.removeVaguely(transition, object);
        }

        @Override
        public synchronized <T> Transaction removeVaguely(Transaction transaction, T object, Class<T> type)
                throws DataSourceException
        {
            return this.object.removeVaguely(transaction, object, type);
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
