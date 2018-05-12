package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.DataSource;
import com.theredpixelteam.kraitudao.DataSourceException;
import com.theredpixelteam.kraitudao.Transaction;
import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlainDatabaseDataSource implements DataSource {
    public PlainDatabaseDataSource(Connection connection, String tableName, DataObjectInterpreter interpreter)
            throws DataSourceException
    {
        this.connection = connection;
        this.tableName = tableName;
        this.interpreter = interpreter;

        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public String getTableName()
    {
        return this.tableName;
    }

    @Override
    public <T> void pull(T object, Class<T> type) throws DataSourceException
    {

    }

    @Override
    public <T> Collection<T> pull(Class<T> type) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Collection<T> pullVaguely(T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public Transaction commit(Transaction transition, Collection<Object> objects) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction commit(Transaction transition, T object, Class<T> type) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction remove(T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction remove(Transaction transition, T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction clear() throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction clear(Transaction transition) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction removeVaguely(T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction removeVaguely(Transaction transition, T object) throws DataSourceException
    {
        return null;
    }

    public static Optional<String> getSQLType(Class<?> type)
    {
        return Optional.ofNullable(MAPPING.get(type));
    }

    private Transaction currentTransaction;

    protected String tableName;

    protected Connection connection;

    protected DataObjectInterpreter interpreter;

    protected static final Map<Class<?>, DataObject> CACHE = new HashMap<>();

    private static final Map<Class<?>, Class<?>> BOXING = new HashMap<Class<?>, Class<?>>() {
        {
            //  Boxed type       |  Unboxed type
            put(Boolean.class,      boolean.class);
            put(Character.class,    char.class);
            put(Short.class,        short.class);
            put(Integer.class,      int.class);
            put(Long.class,         long.class);
            put(Float.class,        float.class);
            put(Double.class,       double.class);
        }
    };

    private static final Map<Class<?>, String> MAPPING = new HashMap<Class<?>, String>() {
        {
            //  Java type        |  SQL type
            put(boolean.class,      "BOOLEAN");
            put(byte.class,         "BINARY(1)");
            put(char.class,         "NCHAR(1)");
            put(short.class,        "SMALLINT");
            put(int.class,          "INTEGER");
            put(long.class,         "BIGINT");
            put(float.class,        "REAL");
            put(double.class,       "FLOAT");
        }
    };

    private class TransactionImpl implements Transaction
    {
        TransactionImpl()
        {
        }

        @Override
        public boolean push() throws DataSourceException
        {
            if(!valid)
                return false;

            try {
                connection.commit();
            } catch (SQLException e) {
                throw new DataSourceException(e);
            }

            destroy();
            return true;
        }

        @Override
        public boolean cancel()
        {
            if(!valid)
                return false;

            try {
                connection.rollback();
            } catch (SQLException e) {
                this.lastException = new DataSourceException(e);
                return false;
            }

            destroy();
            return true;
        }

        @Override
        public Optional<Exception> getLastException()
        {
            return Optional.ofNullable(this.lastException);
        }

        void destroy()
        {
            this.valid = false;
            currentTransaction = null;
        }

        boolean valid()
        {
            return valid;
        }

        private Exception lastException;

        private boolean valid = true;
    }
}
