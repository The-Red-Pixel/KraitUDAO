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
 *
 */

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

    public void waitForTransaction();
}
