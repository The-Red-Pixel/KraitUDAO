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
import com.theredpixelteam.kraitudao.interpreter.StandardDataObjectInterpreter;
import com.theredpixelteam.redtea.function.FunctionWithThrowable;
import com.theredpixelteam.redtea.function.ProcedureWithThrowable;
import com.theredpixelteam.redtea.function.SupplierWithThrowable;
import com.theredpixelteam.redtea.util.Pair;

import java.sql.Connection;
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
    public <T> boolean pull(T object, Class<T> type) throws DataSourceException
    {
        return false;
    }

    @Override
    public <T> Collection<T> pull(Class<T> type) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Collection<T> pullVaguely(T object, Class<T> type)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction commit(Transaction transaction, T object, Class<T> type)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction remove(Transaction transaction, T object, Class<T> type)
            throws DataSourceException
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
            throws DataSourceException
    {
        return null;
    }

    @Override
    public void waitForTransaction()
    {
        while(this.currentTransaction != null);
    }

    public void createTable(Connection conection, Class<?> dataType) throws DataSourceException
    {
        try {
            createTable0(connection, container.interpretIfAbsent(dataType, interpreter), false);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
    }

    public void createTable(Connection connection, DataObject dataObject) throws DataSourceException
    {
        createTable0(connection, dataObject, false);
    }

    public boolean createTableIfNotExists(Connection connection, Class<?> dataType) throws DataSourceException
    {
        try {
            return createTable0(connection, container.interpretIfAbsent(dataType, interpreter), true);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
    }

    public boolean createTableIfNotExists(Connection connection, DataObject dataObject) throws DataSourceException
    {
        return createTable0(connection, dataObject, true);
    }

    private boolean createTable0(Connection connection, DataObject dataObject, boolean ifNotExists) throws DataSourceException
    {

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

    private class Node
    {
        Node(ProcedureWithThrowable<SQLException> procedure)
        {
            this(procedure, new HashMap<>());
        }

        Node(ProcedureWithThrowable<SQLException> procedure, Map<Class<?>, FunctionWithThrowable<String, String, SQLException>> cache)
        {
            this.procedure = procedure;
            this.children = new ArrayList<>();
            this.cache = cache;
        }

        void run() throws SQLException
        {
            procedure.run();

            for(Node node : children)
                node.run();
        }

        void append(ProcedureWithThrowable<SQLException> procedure)
        {
            children.add(new Node(procedure, cache));
        }

        Optional<SupplierWithThrowable<String, SQLException>> getCache(Class<?> type, String tableName)
        {
            FunctionWithThrowable<String, String, SQLException> cached;

            if((cached = cache.get(type)) == null)
                return Optional.empty();

            return Optional.of(() -> cached.apply(tableName));
        }

        final Map<Class<?>, FunctionWithThrowable<String, String, SQLException>> cache;

        final ProcedureWithThrowable<SQLException> procedure;

        final ArrayList<Node> children;
    }

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
