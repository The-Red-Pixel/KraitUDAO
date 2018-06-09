/*
 * DataSourceException.java
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

package com.theredpixelteam.kraitudao;

public class DataSourceException extends Exception {
    public DataSourceException()
    {
    }

    public DataSourceException(String msg)
    {
        super(msg);
    }

    public DataSourceException(Throwable cause)
    {
        super(cause);
    }

    public DataSourceException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public static class Busy extends DataSourceException
    {
        public Busy()
        {
        }

        public Busy(String msg)
        {
            super(msg);
        }

        public Busy(Throwable cause)
        {
            super(cause);
        }

        public Busy(String msg, Throwable cause)
        {
            super(msg, cause);
        }
    }

    public static class UnsupportedValueType extends DataSourceException
    {
        public UnsupportedValueType()
        {
        }

        public UnsupportedValueType(String msg)
        {
            super(msg);
        }

        public UnsupportedValueType(Throwable cause)
        {
            super(cause);
        }

        public UnsupportedValueType(String msg, Throwable cause)
        {
            super(msg, cause);
        }
    }
}
