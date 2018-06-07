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
import com.theredpixelteam.kraitudao.annotations.metadata.common.Precision;
import com.theredpixelteam.kraitudao.annotations.metadata.common.Size;
import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class PlainSQLDatabaseDataSource implements DataSource {
    public PlainSQLDatabaseDataSource(Connection connection,
                                   String tableName,
                                   DataObjectInterpreter interpreter,
                                   DataObjectContainer container)
            throws DataSourceException
    {
        this.connection = connection;
        this.tableName = tableName;
        this.interpreter = interpreter;
        this.container = container;

        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                   String tableName,
                                   DataObjectInterpreter interpreter)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, DataObjectCache.getGlobal());
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

    private void checkTransaction(Transaction transaction) throws DataSourceException
    {
        if(!transaction.equals(this.currentTransaction))
            throw new DataSourceException.DataSourceBusyException();
    }

    private <T> Collection<T> query(Class<T> dataType, List<ValueObject> keys, List<ValueObject> values)
    {

    }

    @Override
    public void waitForTransaction()
    {
        while(this.currentTransaction != null);
    }

    public static Optional<String> getSQLType(Class<?> type)
    {
        return Optional.ofNullable(MAPPING.get(type));
    }

    public int createTable(Connection conection, String tableName, Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        return createTable(conection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    public static int createTable(Connection connection,
                                  String tableName,
                                  DataObject dataObject) throws SQLException
    {
        return createTable0(connection, "CREATE TABLE " + tableName, dataObject);
    }

    public static int createTable(Connection connection,
                                  String tableName,
                                  Class<?> dataType,
                                  DataObjectContainer container,
                                  DataObjectInterpreter interpreter)
            throws SQLException, DataObjectInterpretationException
    {
        return createTable(connection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    public int createTable(Connection connection,
                           DataObject dataObject)
            throws SQLException
    {
        return createTable(connection, tableName, dataObject);
    }

    public int createTable(Connection connection,
                           Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        return createTable(connection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    public int createTableIfNotExists(Connection connection,
                                      Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        return createTableIfNotExists(connection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    public int createTableIfNotExists(Connection connection,
                                      DataObject dataObject)
            throws SQLException
    {
        return createTableIfNotExists(connection, tableName, dataObject);
    }

    public static int createTableIfNotExists(Connection connection,
                                             String tableName,
                                             DataObject dataObject) throws SQLException
    {
        return createTable0(connection, "CREATE TABLE IF NOT EXISTS " + tableName, dataObject);
    }

    public static int createTableIfNotExists(Connection connection,
                                             String tableName,
                                             Class<?> dataType,
                                             DataObjectContainer container,
                                             DataObjectInterpreter interpreter)
            throws SQLException, DataObjectInterpretationException
    {
        return createTableIfNotExists(connection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    private static int createTable0(Connection connection, String statement, DataObject dataObject) throws SQLException
    {
        StringBuilder stmt = new StringBuilder(statement).append(" (");
        StringBuilder constraint = new StringBuilder();

        if(dataObject instanceof UniqueDataObject)
        {
            UniqueDataObject uniqueDataObject = (UniqueDataObject) dataObject;
            ValueObject key = uniqueDataObject.getKey();

            appendTableElement(stmt, key);

            constraint.append(String.format("CONSTRAINT CONSTRAINT_KEY PRIMARY KEY (%s)",
                    key.getName()));
        }
        else if(dataObject instanceof MultipleDataObject)
        {
            MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
            ValueObject primaryKey = multipleDataObject.getPrimaryKey();

            appendTableElement(stmt, primaryKey);


            constraint.append(
                            String.format("CONSTRAINT CONSTRAINT_PRIMARY_KEY PRIMARY KEY (%s)",
                                    primaryKey.getName()))
                    .append("CONSTRAINT CONSTRAINT_SECONDARY_KEYS SECONDARY KEY (");

            Collection<ValueObject> valueObjectCollection = multipleDataObject.getSecondaryKeys().values();
            int valueObjectCollectionSize = valueObjectCollection.size(), i = 0;
            for(ValueObject secondaryKey : multipleDataObject.getSecondaryKeys().values())
            {
                appendTableElement(stmt, secondaryKey);

                constraint.append(secondaryKey.getName());

                if(++i < valueObjectCollectionSize)
                    constraint.append(",");
            }

            constraint.append(")");
        }
        else
            throw new DataObjectException("Illegal or unsupported data object type");

        for(ValueObject valueObject : dataObject.getValues().values())
            appendTableElement(stmt, valueObject);

        stmt.append(constraint).append(")");

        System.out.println(stmt.toString());

        PreparedStatement preparedStatement = connection.prepareStatement(stmt.toString());
        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n;
    }

    private static void appendTableElement(StringBuilder stmt, ValueObject valueObject)
    {
        Class<?> type = tryToUnbox(valueObject.getType());
        String typeString = MAPPING.get(type);

        if(typeString == null)
            throw new DataObjectException("Unsupported type: " + type.getCanonicalName() + " (PLEASE Try to use expandable value)");

        TypeDecorator typeDecorator = TYPE_DECORATORS.get(type);
        if(typeDecorator != null)
            typeString = typeDecorator.decorate(typeString, valueObject);

        stmt.append(String.format(typeString, valueObject.getName())).append(",");
    }

    private static Class<?> tryToUnbox(Class<?> type)
    {
        Class<?> unboxed = BOXING.get(type);

        return unboxed == null ? type : unboxed;
    }

    private static TypeDecorator of(TypeDecorator... decorators)
    {
        final TypeDecorator[] sequence = decorators;
        return (type, valueObject) -> {
            for(TypeDecorator decorator : sequence)
                type = decorator.decorate(type, valueObject);

            return type;
        };
    }

    private volatile Transaction currentTransaction;

    protected String tableName;

    protected Connection connection;

    protected DataObjectInterpreter interpreter;

    protected final DataObjectContainer container;

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
            put(float.class,        "FLOAT");
            put(double.class,       "DOUBLE");
            put(String.class,       "NVARCHAR");
            put(BigDecimal.class,   "DECIMAL");
            put(BigInteger.class,   "BIGINT");
        }
    };

    private static final Map<Class<?>, TypeDecorator> TYPE_DECORATORS = new HashMap<Class<?>, TypeDecorator>() {
        {
            TypeDecorator notNullDecorator = (type, valueObject) -> {
                Optional<NotNull> metadata = valueObject.getMetadata(NotNull.class);

                if(!metadata.isPresent())
                    return type;
                else
                    return type + " NOT NULL";
            };

            TypeDecorator placer = (type, valueObject) -> type + " %s";

            TypeDecorator integerPrecisionDecorator = of((type, valueObject) -> {
                Optional<Precision> metadata = valueObject.getMetadata(Precision.class);

                if(!metadata.isPresent())
                    return type;
                else
                    return type + "(" + metadata.get().integer() + ")";
            }, placer, notNullDecorator);

            TypeDecorator decimalPrecisionDecorator = of((type, valueObject) -> {
                Optional<Precision> metadata = valueObject.getMetadata(Precision.class);

                if(!metadata.isPresent())
                    return type;

                Precision precision = metadata.get();

                return type + "(" + precision.integer() + "," + precision.decimal() + ")";
            }, placer, notNullDecorator);

            put(String.class, of((type, valueObject) -> {
                Optional<Size> metadata = valueObject.getMetadata(Size.class);

                if(!metadata.isPresent())
                    return type;
                else
                    return type + "(" + metadata.get().value() + ")";
            }, placer, notNullDecorator));

            put(short.class, integerPrecisionDecorator);
            put(int.class, integerPrecisionDecorator);
            put(long.class, integerPrecisionDecorator);
            put(BigInteger.class, integerPrecisionDecorator);

            put(float.class, decimalPrecisionDecorator);
            put(double.class, decimalPrecisionDecorator);
            put(BigDecimal.class, decimalPrecisionDecorator);
        }
    };

    private interface TypeDecorator
    {
        String decorate(String type, ValueObject valueObject);
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
}
