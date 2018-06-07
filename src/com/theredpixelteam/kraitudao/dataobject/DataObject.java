/*
 * DataObject.java
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
