/*
 * DefaultDataTypeParser.java
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

package com.theredpixelteam.kraitudao.common.sql;

import com.theredpixelteam.kraitudao.DataSourceError;
import com.theredpixelteam.kraitudao.misc.Misc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DefaultDataTypeParser implements DataTypeParser {
    @Override
    public String parseType(Class<?> dataType)
    {
        String type = MAPPING.get(Misc.tryToUnbox(dataType));

        if(type == null)
            throw new DataSourceError("Unsupported type: " + dataType.getCanonicalName());

        return type;
    }

    @Override
    public boolean supportType(Class<?> dataType)
    {
        return MAPPING.containsKey(Misc.tryToUnbox(dataType));
    }

    private static final Map<Class<?>, String> MAPPING = new HashMap<Class<?>, String>() {
        {
            put(boolean.class,      "BIT");
            put(byte.class,         "TINYINT");
            put(char.class,         "NCHAR(1)");
            put(short.class,        "SMALLINT");
            put(int.class,          "INTEGER");
            put(long.class,         "BIGINT");
            put(float.class,        "REAL");
            put(double.class,       "DOUBLE");
            put(String.class,       "NVARCHAR");
            put(BigDecimal.class,   "DECIMAL");
        }
    };

    public static final DefaultDataTypeParser INSTANCE = new DefaultDataTypeParser();
}
