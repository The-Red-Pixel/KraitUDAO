package com.theredpixelteam.kraitudao.dataobject;

import java.util.Map;
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
