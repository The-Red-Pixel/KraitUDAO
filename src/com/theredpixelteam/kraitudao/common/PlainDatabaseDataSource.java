package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.DataSource;
import com.theredpixelteam.kraitudao.DataSourceException;
import com.theredpixelteam.kraitudao.Transition;
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
    public Transition commit(Transition transition, Collection<Object> objects) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition commit(Transition transition, T object, Class<T> type) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition remove(T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition remove(Transition transition, T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition clear() throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition clear(Transition transition) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition removeVaguely(T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transition removeVaguely(Transition transition, T object) throws DataSourceException
    {
        return null;
    }

    public static Optional<String> getSQLType(Class<?> type)
    {
        return Optional.ofNullable(MAPPING.get(type));
    }

    private Transition currentTransistion;

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

    private static class TransitionImpl implements Transition
    {

        @Override
        public void push() throws DataSourceException
        {

        }

        @Override
        public boolean cancel()
        {
            return false;
        }
    }
}
