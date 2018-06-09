package com.theredpixelteam.kraitudao.common.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultDataArgumentWrapper implements DataArgumentWrapper {
    @Override
    public Optional<DataArgument> wrap(Object object)
    {
        DataArgumentApplier applier;

        Class<?> t = object.getClass();

        BLOCK: {
            do {
                if((applier = MAPPED.get(t)) != null)
                    break BLOCK;
            } while((t = t.getSuperclass()) != null);

            return Optional.empty();
        }

        final DataArgumentApplier confirmedApplier = applier;

        return Optional.of(((p, i) -> confirmedApplier.apply(p, i, object)));
    }

    public static final DefaultDataArgumentWrapper INSTANCE = new DefaultDataArgumentWrapper();

    protected static final Map<Class<?>, DataArgumentApplier> MAPPED = new HashMap<Class<?>, DataArgumentApplier>() {
        {
            put(boolean.class,      (p, i, v) -> p.setBoolean(i, (Boolean) v));
            put(byte.class,         (p, i, v) -> p.setBinaryStream(i, new SingletonInputStream((Byte) v), 1));
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
