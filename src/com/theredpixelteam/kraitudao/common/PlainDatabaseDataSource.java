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

    protected String tableName;

    protected Connection connection;

    protected DataObjectInterpreter interpreter;

    protected static final Map<Class<?>, DataObject> cache = new HashMap<>();

    private static final Map<Class<?>, Class<?>> BOXING = new HashMap<Class<?>, Class<?>>() {
        {
            put(Boolean.class, boolean.class);
            put(Character.class, char.class);
            put(Short.class, short.class);
            put(Integer.class, int.class);
            put(Long.class, long.class);
            put(Float.class, float.class);
            put(Double.class, double.class);
        }
    };

    private static final Map<Class<?>, String> MAPPING = new HashMap<Class<?>, String>() {
        {

        }
    };
}
