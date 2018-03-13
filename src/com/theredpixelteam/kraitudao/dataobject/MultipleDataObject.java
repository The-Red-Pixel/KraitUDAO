package com.theredpixelteam.kraitudao.dataobject;

import java.util.Map;
import java.util.Optional;

public interface MultipleDataObject extends DataObject {
    public ValueObject getPrimaryKey();

    public default boolean isPrimaryKey(String name)
    {
        return getPrimaryKey(name).isPresent();
    }

    public default Optional<ValueObject> getPrimaryKey(String name)
    {
        ValueObject object = getPrimaryKey();

        if(object.getName().equals(name))
            return Optional.of(object);

        return Optional.empty();
    }

    public Map<String, ValueObject> getSecondaryKeys();

    public default boolean isSecondaryKey(String name)
    {
        return getSecondaryKey(name).isPresent();
    }

    public default Optional<ValueObject> getSecondaryKey(String name)
    {
        return Optional.ofNullable(getSecondaryKeys().get(name));
    }
}
