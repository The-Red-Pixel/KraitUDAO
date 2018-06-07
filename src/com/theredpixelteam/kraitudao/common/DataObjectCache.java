/*
 * DataObjectCache.java
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

package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.dataobject.DataObjectContainer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DataObjectCache implements DataObjectContainer {
    public DataObjectCache()
    {
    }

    @Override
    public Optional<DataObject> get(Class<?> type)
    {
        return Optional.ofNullable(cache.get(type));
    }

    @Override
    public boolean remove(Class<?> type)
    {
        return cache.remove(type) != null;
    }

    @Override
    public boolean remove(Class<?> type, DataObject dataObject)
    {
        return cache.remove(type, dataObject);
    }

    @Override
    public Optional<DataObject> put(Class<?> type, DataObject dataObject)
    {
        return Optional.ofNullable(cache.put(type, dataObject));
    }

    public static DataObjectCache getGlobal()
    {
        return GLOBAL;
    }

    private static final DataObjectCache GLOBAL = new DataObjectCache();

    private final Map<Class<?>, DataObject> cache = new ConcurrentHashMap<>();
}
