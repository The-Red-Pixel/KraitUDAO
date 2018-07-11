/*
 * MultipleDataObject.java
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

import com.theredpixelteam.redtea.util.Optional;

import java.util.Map;

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
