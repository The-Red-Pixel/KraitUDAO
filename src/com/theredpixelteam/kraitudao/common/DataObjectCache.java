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
 */

package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.dataobject.DataObjectContainer;
import com.theredpixelteam.kraitudao.dataobject.DataObjectError;
import com.theredpixelteam.kraitudao.dataobject.ValueObject;
import com.theredpixelteam.kraitudao.interpreter.DataObjectExpander;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;

import java.util.Collections;
import java.util.HashMap;
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
    public Optional<Map<String, ValueObject>> expand(ValueObject valueObject, DataObjectExpander expander)
            throws DataObjectInterpretationException
    {
        DataObject dataObject = valueObject.getOwner();
        Class<?> dataType = dataObject.getType();
        Map<String, ValueObject> result;

        if ((result = expansionCache.get(dataType)) != null)
            return Optional.of(result);

        // -- check consistency --
        DataObject cachedDataObject = cache.get(dataType);

        if (cachedDataObject == null)
            cache.put(dataType, dataObject);
        else if (!dataObject.equals(cachedDataObject))
            throw new DataObjectError("DataObject Cache Conflict");
        // -----------------------

        result = expander.expand(valueObject).orElse(null);

        if(result == null)
            return Optional.empty();

        expansionCache.put(dataType, result);

        return Optional.of(result);
    }

    @Override
    public DataObject expand(DataObject dataObject, DataObjectExpander expander)
            throws DataObjectInterpretationException
    {
        return expander.expand(dataObject);
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

    private final Map<Class<?>, DataObject> cache = new HashMap<>();

    private final Map<Class<?>, Map<String, ValueObject>> expansionCache = new HashMap<>();
}
