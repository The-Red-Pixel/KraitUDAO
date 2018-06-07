/*
 * DataObjectContainer.java
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

package com.theredpixelteam.kraitudao.dataobject;

import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;

import java.util.Optional;

public interface DataObjectContainer {
    public Optional<DataObject> get(Class<?> type);

    public default DataObject interpret(Class<?> type, DataObjectInterpreter interpreter)
            throws DataObjectInterpretationException
    {
        DataObject dataObject = interpreter.get(type);
        put(type, dataObject);
        return dataObject;
    }

    public default DataObject interpretIfAbsent(Class<?> type, DataObjectInterpreter interpreter)
            throws DataObjectInterpretationException
    {
        return get(type).orElse(interpret(type, interpreter));
    }
    public boolean remove(Class<?> type);

    public boolean remove(Class<?> type, DataObject dataObject);

    public default boolean contains(Class<?> type)
    {
        return get(type).isPresent();
    }

    public Optional<DataObject> put(Class<?> type, DataObject dataObject);
}
