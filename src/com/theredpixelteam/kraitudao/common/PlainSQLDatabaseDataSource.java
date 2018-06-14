/*
 * PlainSQLDatabaseDataSource.java
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

package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.DataSource;
import com.theredpixelteam.kraitudao.DataSourceException;
import com.theredpixelteam.kraitudao.Transaction;
import com.theredpixelteam.kraitudao.common.sql.*;
import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;
import com.theredpixelteam.kraitudao.interpreter.DataObjectMalformationException;
import com.theredpixelteam.kraitudao.interpreter.StandardDataObjectInterpreter;
import com.theredpixelteam.redtea.util.Pair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlainSQLDatabaseDataSource implements DataSource {
    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectContainer container,
                                      DataArgumentWrapper argumentWrapper,
                                      DataExtractorFactory extractorFactory)
            throws DataSourceException
    {
        this.connection = connection;
        this.tableName = tableName;
        this.interpreter = interpreter;
        this.container = container;
        this.argumentWrapper = argumentWrapper;
        this.extractorFactory = extractorFactory;

        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectContainer container)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, container, DefaultDataArgumentWrapper.INSTANCE, DefaultDataExtractorFactory.INSTANCE);
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, DataObjectCache.getGlobal());
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName)
            throws DataSourceException
    {
        this(connection, tableName, StandardDataObjectInterpreter.INSTANCE, DataObjectCache.getGlobal());
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public String getTableName()
    {
        return this.tableName;
    }

    private boolean putArgument0(List<Pair<String, DataArgument>> list, Object object, ValueObject valueObject, String msgOnNull, boolean nonNull)
            throws DataSourceException
    {
        Object value = valueObject.get(object);

        if(value == null)
            if(nonNull)
                throw new DataSourceException(msgOnNull);
            else
                return false;
        else
            list.add(Pair.of(valueObject.getName(), argumentWrapper.wrap(object)
                    .orElseThrow(() -> new DataSourceException.UnsupportedValueType(valueObject.getType().getCanonicalName()))));

        return true;
    }

    private void putArgument(List<Pair<String, DataArgument>> list, Object object, ValueObject valueObject, String msgOnNull)
            throws DataSourceException
    {
        putArgument0(list, object, valueObject, msgOnNull, true);
    }

    private boolean putArgumentIfNotNull(List<Pair<String, DataArgument>> list, Object object, ValueObject valueObject)
            throws DataSourceException
    {
        return putArgument0(list, object, valueObject, null, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void pull(T object, Class<T> type) throws DataSourceException, DataObjectInterpretationException
    {
        DataObject dataObject = container.interpretIfAbsent(type, interpreter);

        List<Pair<String, DataArgument>> keys = new ArrayList<>();

        if(dataObject instanceof UniqueDataObject)
        {
            UniqueDataObject uniqueDataObject = (UniqueDataObject) dataObject;
            ValueObject key = uniqueDataObject.getKey();

            putArgument(keys, object, key, "Null key");
        }
        else if(dataObject instanceof MultipleDataObject)
        {
            MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
            ValueObject primarykey = multipleDataObject.getPrimaryKey();
            Collection<ValueObject> secondaryKeys = multipleDataObject.getSecondaryKeys().values();

            putArgument(keys, object, primarykey, "Null primary key");
            for(ValueObject secondaryKey : secondaryKeys)
                putArgument(keys, object, secondaryKey, "Null secondary key");
        }
        else
            throw new DataObjectMalformationException.IllegalType();

        Map<String, ValueObject> values = dataObject.getValues();
        Collection<String> valueNames = values.keySet();

        Pair<String, DataArgument>[] keyArray = keys.toArray(new Pair[keys.size()]);
        String[] nameArray = valueNames.toArray(new String[valueNames.size()]);

        try {
            ResultSet resultSet = manipulator.query(connection, tableName, keyArray, nameArray);

            resultSet.last();

            if(resultSet.getRow() != 1)
                throw new DataSourceException("Multiple record found");

            for(ValueObject valueObject : values.values())
                valueObject.set(object, extractorFactory.create(valueObject.getType(), valueObject.getName())
                        .orElseThrow(() -> new DataSourceException.UnsupportedValueType(valueObject.getType().getCanonicalName()))
                        .extract(resultSet));

            resultSet.close();
        } catch (SQLException e) {
            throw new DataSourceException("SQLException", e);
        }
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
    public <T> Transaction commit(Transaction transition, T object, Class<T> type) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction remove(Transaction transition, T object) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction clear(Transaction transition) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction removeVaguely(Transaction transition, T object) throws DataSourceException
    {
        return null;
    }

    private void checkTransaction(Transaction transaction) throws DataSourceException
    {
        if((transaction == null && this.currentTransaction != null) || !transaction.equals(this.currentTransaction))
            throw new DataSourceException.Busy();
    }

    @Override
    public void waitForTransaction()
    {
        while(this.currentTransaction != null);
    }

    public void createTable(Connection conection, String tableName, Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        manipulator.createTable(conection, tableName, dataType, container, interpreter);
    }

    public void createTable(Connection connection,
                           DataObject dataObject)
            throws SQLException
    {
        manipulator.createTable(connection, tableName, dataObject);
    }

    public void createTable(Connection connection,
                           Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        manipulator.createTable(connection, tableName, dataType, container, interpreter);
    }

    public boolean createTableIfNotExists(Connection connection,
                                      Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        return manipulator.createTableIfNotExists(connection, tableName, dataType, container, interpreter);
    }

    public boolean createTableIfNotExists(Connection connection,
                                      DataObject dataObject)
            throws SQLException
    {
        return manipulator.createTableIfNotExists(connection, tableName, dataObject);
    }

    public DatabaseManipulator getManipulator()
    {
        return manipulator;
    }

    public void setManipulator(DatabaseManipulator manipulator)
    {
        this.manipulator = Objects.requireNonNull(manipulator);
    }

    public DataArgumentWrapper getArgumentWrapper()
    {
        return argumentWrapper;
    }

    public void setArgumentWrapper(DataArgumentWrapper argumentWrapper)
    {
        this.argumentWrapper = Objects.requireNonNull(argumentWrapper);
    }

    private volatile Transaction currentTransaction;

    protected String tableName;

    protected Connection connection;

    protected DataObjectInterpreter interpreter;

    protected final DataObjectContainer container;

    protected DatabaseManipulator manipulator;

    protected DataArgumentWrapper argumentWrapper;

    protected DataExtractorFactory extractorFactory;

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
