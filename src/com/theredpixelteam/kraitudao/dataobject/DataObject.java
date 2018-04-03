package com.theredpixelteam.kraitudao.dataobject;

import java.util.Map;
import java.util.Optional;

public interface DataObject {
    public default boolean hasValue(String name)
    {
        return getValue(name).isPresent();
    }

    public default <T> boolean hasValue(String name, Class<T> type)
    {
        return getValue(name, type).isPresent();
    }

    public Optional<ValueObject> getValue(String name);

    public Optional<ValueObject> getValueObject(String name);

    public default boolean hasValueObject(String name)
    {
        return getValueObject(name).isPresent();
    }

    public default <T> boolean hasValueObject(String name, Class<T> type)
    {
        return getValueObject(name, type).isPresent();
    }

    public default <T> Optional<ValueObject> getValueObject(String name, Class<T> type)
    {
        Optional<ValueObject> optional = getValueObject(name);

        if(!optional.isPresent())
            return optional;

        ValueObject valueObject = optional.get();

        if(!valueObject.getType().equals(type))
            return Optional.empty();

        return Optional.of(valueObject);
    }

    public default <T> Optional<ValueObject> getValue(String name, Class<T> type)
    {
        Optional<ValueObject> optional = getValue(name);

        if(!optional.isPresent())
            return optional;

        ValueObject valueObject = optional.get();

        if(!valueObject.getType().equals(type))
            return Optional.empty();

        return Optional.of(valueObject);
    }

    public Map<String, ValueObject> getValues();

    public Class<?> getType();
}
