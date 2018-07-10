/*
 * DefaultDataExtractorFactory.java
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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultDataExtractorFactory implements DataExtractorFactory {
    @Override
    public Optional<DataExtractor> create(Class<?> type, String columnName)
    {
        DirectableDataExtractor directableDataExtractor = MAPPED.get(Misc.tryToUnbox(type));

        if(directableDataExtractor == null)
            return Optional.empty();

        return Optional.of(resultSet -> directableDataExtractor.extract(resultSet, columnName, 0, false));
    }

    @Override
    public Optional<DataExtractor> create(Class<?> type, int columnIndex)
    {
        DirectableDataExtractor directableDataExtractor = MAPPED.get(Misc.tryToUnbox(type));

        if(directableDataExtractor == null)
            return Optional.empty();

        return Optional.of(resultSet -> directableDataExtractor.extract(resultSet, null, columnIndex, true));
    }

    private static char readSilently(Reader reader)
    {
        try {
            int i = reader.read();

            if(i < 0)
                throw new EOFException();

            return (char) i;
        } catch (IOException e) {
            throw new DataSourceError(e);
        }
    }

    public static final DefaultDataExtractorFactory INSTANCE = new DefaultDataExtractorFactory();

    private static final Map<Class<?>, DirectableDataExtractor> MAPPED = new HashMap<Class<?>, DirectableDataExtractor>()
    {
        {
            put(boolean.class,      (r, n, i, u) -> u ? r.getBoolean(i) : r.getBoolean(n));
            put(byte.class,         (r, n, i, u) -> u ? r.getByte(i) : r.getByte(n));
            put(char.class,         (r, n, i, u) -> readSilently(u ? r.getNCharacterStream(i) : r.getNCharacterStream(n)));
            put(short.class,        (r, n, i, u) -> u ? r.getShort(i) : r.getShort(n));
            put(int.class,          (r, n, i, u) -> u ? r.getInt(i) : r.getInt(n));
            put(long.class,         (r, n, i, u) -> u ? r.getLong(i) : r.getLong(n));
            put(float.class,        (r, n, i, u) -> u ? r.getFloat(i) : r.getFloat(n));
            put(double.class,       (r, n, i, u) -> u ? r.getDouble(i) : r.getDouble(n));
            put(String.class,       (r, n, i, u) -> u ? r.getNString(i) : r.getNString(n));
            put(BigDecimal.class,   (r, n, i, u) -> u ? r.getBigDecimal(i) : r.getBigDecimal(n));
        }
    };

    private static interface DirectableDataExtractor
    {
        Object extract(ResultSet resultSet, String name, int index, boolean usingIndex) throws SQLException;
    }
}
