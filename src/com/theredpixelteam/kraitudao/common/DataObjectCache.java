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
