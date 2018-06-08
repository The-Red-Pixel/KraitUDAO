/*
 * DefaultDatabaseManipulator.java
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

import com.theredpixelteam.kraitudao.annotations.metadata.common.NotNull;
import com.theredpixelteam.kraitudao.annotations.metadata.common.Precision;
import com.theredpixelteam.kraitudao.annotations.metadata.common.Size;
import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.redtea.util.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultDatabaseManipulator implements DatabaseManipulator {
    @Override
    public ResultSet query(Connection connection, String tableName, Pair<String, DataArgument>[] keys, String[] values)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT " + combine(values, ",", "*") +
                        " FROM " + tableName +
                        " WHERE " + narrow(keys));

        for(int i = 0; i < keys.length;)
            keys[i].second().set(preparedStatement, ++i);

        return preparedStatement.executeQuery();
    }

    @Override
    public int delete(Connection connection, String tableName, Pair<String, DataArgument>[] keysAndValues)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM " + tableName +
                        " WHERE " + narrow(keysAndValues));

        for(int i = 0; i < keysAndValues.length;)
            keysAndValues[i].second().set(preparedStatement, ++i);

        return preparedStatement.executeUpdate();
    }

    @Override
    public int insert(Connection connection, String tableName, Pair<String, DataArgument>[] values)
            throws SQLException
    {
        if(values == null || values.length == 0)
            return 0;

        PreparedStatement preparedStatement = connection.prepareStatement(
                "MERGE INTO " + tableName +
                        " KEY (" + combine(values, ",", null) + ")" +
                        " VALUES (" + arguments(values.length) + ")");

        for(int i = 0; i < values.length;)
            values[i].second().set(preparedStatement, ++i);

        return preparedStatement.executeUpdate();
    }

    @Override
    public void createTable(Connection connection, String tableName, DataObject dataObject)
            throws SQLException {
        createTable0(connection, "CREATE TABLE " + tableName, dataObject);
    }

    @Override
    public boolean createTableIfNotExists(Connection connection, String tableName, DataObject dataObject)
            throws SQLException {
        return createTable0(connection, "CREATE TABLE IF NOT EXISTS " + tableName, dataObject) != 0;
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

            constraint.append("CONSTRAINT CONSTRAINT_KEY PRIMARY KEY (").append(key.getName()).append(")");
        }
        else if(dataObject instanceof MultipleDataObject)
        {
            MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
            ValueObject primaryKey = multipleDataObject.getPrimaryKey();

            appendTableElement(stmt, primaryKey);


            constraint.append("CONSTRAINT CONSTRAINT_PRIMARY_KEY PRIMARY KEY (").append(primaryKey.getName()).append(")")
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

    private static void appendTableElement(StringBuilder stmt, ValueObject valueObject)
    {
        Class<?> type = tryToUnbox(valueObject.getType());
        String typeString = MAPPING.get(type);

        if(typeString == null)
            throw new DataObjectException("Unsupported type: " + type.getCanonicalName() + " (PLEASE Try to use expandable value)");

        TypeDecorator typeDecorator = TYPE_DECORATORS.get(type);
        if(typeDecorator != null)
            typeString = typeDecorator.decorate(typeString, valueObject);

        stmt.append(valueObject.getName()).append(" ").append(typeString).append(",");
    }

    protected static String arguments(int count)
    {
        count -= 1;

        StringBuilder str = new StringBuilder();

        for(int i = 0; i < count; i++)
            str.append("?,");
        str.append("?");

        return str.toString();
    }

    protected static String combine(Object[] objs, String connector, String orElse)
    {
        return combine(objs, connector, "", orElse);
    }

    protected static String combine(Object[] objs, String connector, String lastConnector, String orElse)
    {
        if(objs == null || objs.length == 0)
            return orElse;

        StringBuilder str = new StringBuilder();

        for(int i = 0; i < objs.length - 1; i++)
            str.append(objs[i].toString()).append(connector);
        str.append(objs[objs.length - 1].toString()).append(lastConnector);

        return str.toString();
    }

    protected static String narrow(Pair<String, ?>[] narrows)
    {
        if(narrows == null || narrows.length == 0)
            return "TRUE";

        StringBuilder stmt = new StringBuilder();

        for(int i = 0; i < narrows.length - 1; i++)
            stmt.append(narrows[i].first()).append("=? AND ");
        stmt.append(narrows[narrows.length - 1].first()).append("=?");

        return stmt.toString();
    }

    public static final DatabaseManipulator INSTANCE = new DefaultDatabaseManipulator();

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

            TypeDecorator integerPrecisionDecorator = of((type, valueObject) -> {
                Optional<Precision> metadata = valueObject.getMetadata(Precision.class);

                if(!metadata.isPresent())
                    return type;
                else
                    return type + "(" + metadata.get().integer() + ")";
            }, notNullDecorator);

            TypeDecorator decimalPrecisionDecorator = of((type, valueObject) -> {
                Optional<Precision> metadata = valueObject.getMetadata(Precision.class);

                if(!metadata.isPresent())
                    return type;

                Precision precision = metadata.get();

                return type + "(" + precision.integer() + "," + precision.decimal() + ")";
            }, notNullDecorator);

            put(String.class, of((type, valueObject) -> {
                Optional<Size> metadata = valueObject.getMetadata(Size.class);

                if(!metadata.isPresent())
                    return type;
                else
                    return type + "(" + metadata.get().value() + ")";
            }, notNullDecorator));

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
}
