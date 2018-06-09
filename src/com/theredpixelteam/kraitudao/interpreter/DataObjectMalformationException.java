/*
 * DataObjectMalformationException.java
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

package com.theredpixelteam.kraitudao.interpreter;

public class DataObjectMalformationException extends DataObjectInterpretationException {
    public DataObjectMalformationException()
    {
    }

    public DataObjectMalformationException(String msg)
    {
        super(msg);
    }

    public DataObjectMalformationException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectMalformationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public static class IllegalType extends DataObjectMalformationException
    {
        public IllegalType()
        {
        }

        public IllegalType(String msg)
        {
            super(msg);
        }

        public IllegalType(Throwable cause)
        {
            super(cause);
        }

        public IllegalType(String msg, Throwable cause)
        {
            super(msg, cause);
        }
    }
}
