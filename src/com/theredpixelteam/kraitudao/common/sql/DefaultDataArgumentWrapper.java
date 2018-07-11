/*
 * DefaultDataArgumentWrapper.java
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

import com.theredpixelteam.kraitudao.misc.Misc;
import com.theredpixelteam.redtea.util.Optional;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DefaultDataArgumentWrapper implements DataArgumentWrapper {
    @Override
    public Optional<DataArgument> wrap(Object object)
    {
        DataArgumentApplier applier;

        Class<?> t = Misc.tryToUnbox(object.getClass());

        BLOCK: {
            do {
                if((applier = MAPPED.get(t)) != null)
                    break BLOCK;
            } while((t = t.getSuperclass()) != null);

            return Optional.empty();
        }

        return Optional.of(new DataArgumentImpl(object, applier));
    }

    public static final DefaultDataArgumentWrapper INSTANCE = new DefaultDataArgumentWrapper();

    protected static final Map<Class<?>, DataArgumentApplier> MAPPED = new HashMap<Class<?>, DataArgumentApplier>() {
        {
            put(boolean.class,      (p, i, v) -> p.setBoolean(i, (Boolean) v));
            put(byte.class,         (p, i, v) -> p.setByte(i, (Byte) v));
            put(char.class,         (p, i, v) -> p.setNCharacterStream(i, new SingletonReader((Character) v), 1));
            put(short.class,        (p, i, v) -> p.setShort(i, (Short) v));
            put(int.class,          (p, i, v) -> p.setInt(i, (Integer) v));
            put(long.class,         (p, i, v) -> p.setLong(i, (Long) v));
            put(float.class,        (p, i, v) -> p.setFloat(i, (Float) v));
            put(double.class,       (p, i, v) -> p.setDouble(i, (Double) v));
            put(String.class,       (p, i, v) -> p.setNString(i, (String) v));
            put(BigDecimal.class,   (p, i, v) -> p.setBigDecimal(i, (BigDecimal) v));
        }
    };

    private static class DataArgumentImpl implements DataArgument
    {
        DataArgumentImpl(Object value, DataArgumentApplier applier)
        {
            this.value = value;
            this.applier = applier;
        }

        @Override
        public void apply(PreparedStatement preparedStatement, int index) throws SQLException
        {
            this.applier.apply(preparedStatement, index, value);
        }

        @Override
        public Object getValue()
        {
            return value;
        }

        private final DataArgumentApplier applier;

        private final Object value;
    }

    private static interface DataArgumentApplier
    {
        public void apply(PreparedStatement preparedStatement, int index, Object value) throws SQLException;
    }

    private static class SingletonInputStream extends InputStream
    {
        SingletonInputStream(byte b)
        {
            this.b = b;
        }

        @Override
        public int read()
        {
            if(readed)
                return -1;

            readed = true;
            return b;
        }

        private volatile boolean readed;

        private final byte b;
    }

    private static class SingletonReader extends Reader
    {
        SingletonReader(char c)
        {
            this.c = c;
        }

        @Override
        public int read(char[] cbuf, int off, int len)
        {
            if(readed)
                return -1;

            readed = true;
            cbuf[0] = c;
            return 1;
        }

        @Override
        public void close()
        {
        }

        private volatile boolean readed;

        private final char c;
    }
}
