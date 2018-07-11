/*
 * DataObjectException.java
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

public class DataObjectException extends RuntimeException {
    public DataObjectException()
    {
    }

    public DataObjectException(String msg)
    {
        super(msg);
    }

    public DataObjectException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public static DataObjectException IncapableType(Class<?> current, Class<?> expected)
    {
        return new DataObjectException("Incapable type: " + current.getCanonicalName() + "(" + expected.getCanonicalName() + " expected)");
    }

    public static DataObjectException IncapableValue(Object value, Class<?> expected)
    {
        return new DataObjectException("Incapable value: Type of " + value.getClass().getCanonicalName() + "(" + expected.getCanonicalName() + " expected)");
    }
}
