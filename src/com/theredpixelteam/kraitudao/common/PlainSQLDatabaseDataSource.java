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
import com.theredpixelteam.kraitudao.annotations.metadata.common.NotNull;
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
                                      DatabaseManipulator databaseManipulator,
                                      DataArgumentWrapper argumentWrapper,
                                      DataExtractorFactory extractorFactory)
            throws DataSourceException
    {
        this.connection = connection;
        this.tableName = tableName;
        this.interpreter = interpreter;
        this.container = container;
        this.manipulator = databaseManipulator;
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
                                      DataObjectContainer container,
                                      DatabaseManipulator databaseManipulator)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, container, databaseManipulator, DefaultDataArgumentWrapper.INSTANCE, DefaultDataExtractorFactory.INSTANCE);
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectContainer container)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, container, DefaultDatabaseManipulator.INSTANCE);
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

    private void checkTransaction(Transaction transaction) throws DataSourceException
    {
        if((transaction == null && this.currentTransaction != null)
                || (transaction != null && !transaction.equals(this.currentTransaction)))
            throw new DataSourceException.Busy();
    }

    @Override
    public <T> boolean pull(T object, Class<T> type) throws DataSourceException, DataObjectInterpretationException
    {
        return false;
    }

    @Override
    public <T> Collection<T> pull(Class<T> type) throws DataSourceException, DataObjectInterpretationException
    {
        return null;
    }

    @Override
    public <T> Collection<T> pullVaguely(T object, Class<T> type)
            throws DataSourceException, DataObjectInterpretationException
    {
        return null;
    }

    @Override
    public <T> Transaction commit(Transaction transaction, T object, Class<T> type)
            throws DataSourceException, DataObjectInterpretationException
    {
        return null;
    }

    @Override
    public <T> Transaction remove(Transaction transaction, T object, Class<T> type)
            throws DataSourceException, DataObjectInterpretationException
    {
        return null;
    }

    @Override
    public Transaction clear(Transaction transaction) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction removeVaguely(Transaction transaction, T object, Class<T> type)
            throws DataSourceException, DataObjectInterpretationException
    {
        return null;
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

    public void setExtractorFactory(DataExtractorFactory extractorFactory)
    {
        this.extractorFactory = Objects.requireNonNull(extractorFactory);
    }

    public DataExtractorFactory getExtractorFactory()
    {
        return extractorFactory;
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

    private static class KeyInjection
    {
        KeyInjection()
        {
        }

        void inject(Object object)
        {
            for(Pair<ValueObject, Object> entry : injectiveElements)
                entry.first().set(object, entry.second());
        }

        private final List<Pair<ValueObject, Object>> injectiveElements = new ArrayList<>();
    }
}
