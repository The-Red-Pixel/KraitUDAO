/*
 * DataSource.java
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

package com.theredpixelteam.kraitudao;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface DataSource {
    public default <T> void pull(T object) throws DataSourceException
    {
        pull(object, (Class<T>) object.getClass());
    }

    public <T> boolean pull(T object, Class<T> type) throws DataSourceException;

    public <T> boolean pull(T object, Class<T> type, Class<?>... signatured) throws DataSourceException;

    public default <T> Collection<T> pull(Class<T> type) throws DataSourceException
    {
        return pull(type, ObjectConstructor.of(type, type::newInstance));
    }

    public <T> Collection<T> pull(Class<T> type, ObjectConstructor<T> constructor) throws DataSourceException;

    public <T> Collection<T> pull(Class<T> type, ObjectConstructor<T> constructor, Class<?>... signatured) throws DataSourceException;

    public default <T> Collection<T> pullVaguely(T object) throws DataSourceException
    {
        return pullVaguely(object, (Class<T>)object.getClass());
    }

    public default <T> Collection<T> pullVaguely(T object, Class<T> type) throws DataSourceException
    {
        return pullVaguely(object, type, ObjectConstructor.of(type, type::newInstance));
    }

    public <T> Collection<T> pullVaguely(T object, Class<T> type, ObjectConstructor<T> constructor) throws DataSourceException;

    public default <T> Transaction commit(T object) throws DataSourceException
    {
        return commit(null, object);
    }

    public default <T> void commitInstantly(T object) throws DataSourceException
    {
        commit(object).push();
    }

    public default <T> Transaction commit(Transaction transaction, T object) throws DataSourceException
    {
        return commit(transaction, object, (Class<T>)object.getClass());
    }

    public default <T> void commitInstantly(Transaction transaction, T object) throws DataSourceException
    {
        commit(transaction, object).push();
    }

    public default <T> Transaction commit(T object, Class<T> type) throws DataSourceException
    {
        return commit(null, object, type);
    }

    public default <T> Transaction commit(T object, Class<T> type, Class<?>... signatured) throws DataSourceException
    {
        return commit(null, object, type, signatured);
    }

    public default <T> void commitInstantly(T object, Class<T> type) throws DataSourceException
    {
        commit(object, type).push();
    }

    public default <T> void commitInstantly(T object, Class<T> type, Class<?>... signatured) throws DataSourceException
    {
        commit(object, type, signatured).push();
    }

    public <T> Transaction commit(Transaction transaction, T object, Class<T> type) throws DataSourceException;

    public <T> Transaction commit(Transaction transaction, T object, Class<T> type, Class<?>... signatured) throws DataSourceException;

    public default <T> void commitInstantly(Transaction transaction, T object, Class<T> type) throws DataSourceException
    {
        commit(transaction, object, type).push();
    }

    public default <T> Transaction remove(T object) throws DataSourceException
    {
        return remove(null, object);
    }

    public default <T> void removeInstantly(T object) throws DataSourceException
    {
        remove(object).push();
    }

    public default <T> Transaction remove(T object, Class<T> type) throws DataSourceException
    {
        return remove(null, object, type);
    }

    public default <T> void removeInstantly(T object, Class<T> type) throws DataSourceException
    {
        remove(object, type).push();
    }

    public default <T> Transaction remove(Transaction transaction, T object) throws DataSourceException
    {
        return remove(transaction, object, (Class<T>)object.getClass());
    }

    public default <T> void removeInstantly(Transaction transaction, T object) throws DataSourceException
    {
        remove(transaction, object).push();
    }

    public <T> Transaction remove(Transaction transaction, T object, Class<T> type) throws DataSourceException;

    public default <T> void removeInstantly(Transaction transaction, T object, Class<T> type) throws DataSourceException
    {
        remove(transaction, object, type).push();
    }

    public default Transaction clear() throws DataSourceException
    {
        return clear(null);
    }

    public default void clearInstantly() throws DataSourceException
    {
        clear().push();
    }

    public Transaction clear(Transaction transaction) throws DataSourceException;

    public default void clearInstantly(Transaction transaction) throws DataSourceException
    {
        clear(transaction).push();
    }

    public default <T> Transaction removeVaguely(T object) throws DataSourceException
    {
        return removeVaguely(null, object);
    }

    public default<T> Transaction removeVaguely(T object, Class<T> type) throws DataSourceException
    {
        return removeVaguely(null, object, type);
    }

    public default <T> Transaction removeVaguely(Transaction transaction, T object) throws DataSourceException
    {
        return removeVaguely(transaction, object, (Class<T>)object.getClass());
    }

    public <T> Transaction removeVaguely(Transaction transaction, T object, Class<T> type) throws DataSourceException;

    public void waitForTransaction();
}
