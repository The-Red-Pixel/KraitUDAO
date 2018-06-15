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
import com.theredpixelteam.kraitudao.misc.TypeUtil;
import com.theredpixelteam.redtea.util.Pair;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DefaultDatabaseManipulator implements DatabaseManipulator {
    @Override
    public ResultSet query(Connection connection, String tableName, Pair<String, DataArgument>[] keys, String[] values)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT " + combine(values, ",", "*") +
                        " FROM " + tableName +
                        " WHERE " + narrow(keys));

        if(keys != null)
            for(int i = 0; i < keys.length;)
                keys[i].second().apply(preparedStatement, ++i);

        return preparedStatement.executeQuery();
    }

    @Override
    public int delete(Connection connection, String tableName, Pair<String, DataArgument>[] keysAndValues)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM " + tableName +
                        " WHERE " + narrow(keysAndValues));

        if(keysAndValues != null)
            for(int i = 0; i < keysAndValues.length;)
                keysAndValues[i].second().apply(preparedStatement, ++i);

        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n;
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
            values[i].second().apply(preparedStatement, ++i);

        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n;
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

        PreparedStatement preparedStatement = connection.prepareStatement(stmt.toString());
        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n;
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
        Class<?> type = TypeUtil.tryToUnbox(valueObject.getType());
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

            put(float.class, decimalPrecisionDecorator);
            put(double.class, decimalPrecisionDecorator);
            put(BigDecimal.class, decimalPrecisionDecorator);
        }
    };

    private interface TypeDecorator
    {
        String decorate(String type, ValueObject valueObject);
    }

    protected static class ResultSetFromDisposableStatement implements ResultSet {
        protected ResultSetFromDisposableStatement(ResultSet resultSet)
        {
            this.resultSet = resultSet;
        }

        @Override
        public boolean next() throws SQLException
        {
            return resultSet.next();
        }

        @Override
        public void close() throws SQLException
        {
            resultSet.getStatement().close();
        }

        @Override
        public boolean wasNull() throws SQLException
        {
            return resultSet.wasNull();
        }

        @Override
        public String getString(int columnIndex) throws SQLException
        {
            return resultSet.getString(columnIndex);
        }

        @Override
        public boolean getBoolean(int columnIndex) throws SQLException
        {
            return resultSet.getBoolean(columnIndex);
        }

        @Override
        public byte getByte(int columnIndex) throws SQLException
        {
            return resultSet.getByte(columnIndex);
        }

        @Override
        public short getShort(int columnIndex) throws SQLException
        {
            return resultSet.getShort(columnIndex);
        }

        @Override
        public int getInt(int columnIndex) throws SQLException
        {
            return resultSet.getInt(columnIndex);
        }

        @Override
        public long getLong(int columnIndex) throws SQLException
        {
            return resultSet.getLong(columnIndex);
        }

        @Override
        public float getFloat(int columnIndex) throws SQLException
        {
            return resultSet.getFloat(columnIndex);
        }

        @Override
        public double getDouble(int columnIndex) throws SQLException
        {
            return resultSet.getDouble(columnIndex);
        }

        @Deprecated
        @Override
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
        {
            return resultSet.getBigDecimal(columnIndex, scale);
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException
        {
            return resultSet.getBytes(columnIndex);
        }

        @Override
        public Date getDate(int columnIndex) throws SQLException
        {
            return resultSet.getDate(columnIndex);
        }

        @Override
        public Time getTime(int columnIndex) throws SQLException
        {
            return resultSet.getTime(columnIndex);
        }

        @Override
        public Timestamp getTimestamp(int columnIndex) throws SQLException
        {
            return resultSet.getTimestamp(columnIndex);
        }

        @Override
        public InputStream getAsciiStream(int columnIndex) throws SQLException
        {
            return resultSet.getAsciiStream(columnIndex);
        }

        @Deprecated
        @Override
        public InputStream getUnicodeStream(int columnIndex) throws SQLException
        {
            return resultSet.getUnicodeStream(columnIndex);
        }

        @Override
        public InputStream getBinaryStream(int columnIndex) throws SQLException
        {
            return resultSet.getBinaryStream(columnIndex);
        }

        @Override
        public String getString(String columnLabel) throws SQLException
        {
            return resultSet.getString(columnLabel);
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException
        {
            return resultSet.getBoolean(columnLabel);
        }

        @Override
        public byte getByte(String columnLabel) throws SQLException
        {
            return resultSet.getByte(columnLabel);
        }

        @Override
        public short getShort(String columnLabel) throws SQLException
        {
            return resultSet.getShort(columnLabel);
        }

        @Override
        public int getInt(String columnLabel) throws SQLException
        {
            return resultSet.getInt(columnLabel);
        }

        @Override
        public long getLong(String columnLabel) throws SQLException
        {
            return resultSet.getLong(columnLabel);
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException
        {
            return resultSet.getFloat(columnLabel);
        }

        @Override
        public double getDouble(String columnLabel) throws SQLException
        {
            return resultSet.getDouble(columnLabel);
        }

        @Deprecated
        @Override
        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException
        {
            return resultSet.getBigDecimal(columnLabel, scale);
        }

        @Override
        public byte[] getBytes(String columnLabel) throws SQLException
        {
            return resultSet.getBytes(columnLabel);
        }

        @Override
        public Date getDate(String columnLabel) throws SQLException
        {
            return resultSet.getDate(columnLabel);
        }

        @Override
        public Time getTime(String columnLabel) throws SQLException
        {
            return resultSet.getTime(columnLabel);
        }

        @Override
        public Timestamp getTimestamp(String columnLabel) throws SQLException
        {
            return resultSet.getTimestamp(columnLabel);
        }

        @Override
        public InputStream getAsciiStream(String columnLabel) throws SQLException
        {
            return resultSet.getAsciiStream(columnLabel);
        }

        @Deprecated
        @Override
        public InputStream getUnicodeStream(String columnLabel) throws SQLException
        {
            return resultSet.getUnicodeStream(columnLabel);
        }

        @Override
        public InputStream getBinaryStream(String columnLabel) throws SQLException
        {
            return resultSet.getBinaryStream(columnLabel);
        }

        @Override
        public SQLWarning getWarnings() throws SQLException
        {
            return resultSet.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException
        {
            resultSet.clearWarnings();
        }

        @Override
        public String getCursorName() throws SQLException
        {
            return resultSet.getCursorName();
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException
        {
            return resultSet.getMetaData();
        }

        @Override
        public Object getObject(int columnIndex) throws SQLException
        {
            return resultSet.getObject(columnIndex);
        }

        @Override
        public Object getObject(String columnLabel) throws SQLException
        {
            return resultSet.getObject(columnLabel);
        }

        @Override
        public int findColumn(String columnLabel) throws SQLException
        {
            return resultSet.findColumn(columnLabel);
        }

        @Override
        public Reader getCharacterStream(int columnIndex) throws SQLException
        {
            return resultSet.getCharacterStream(columnIndex);
        }

        @Override
        public Reader getCharacterStream(String columnLabel) throws SQLException
        {
            return resultSet.getCharacterStream(columnLabel);
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException
        {
            return resultSet.getBigDecimal(columnIndex);
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel) throws SQLException
        {
            return resultSet.getBigDecimal(columnLabel);
        }

        @Override
        public boolean isBeforeFirst() throws SQLException
        {
            return resultSet.isBeforeFirst();
        }

        @Override
        public boolean isAfterLast() throws SQLException
        {
            return resultSet.isAfterLast();
        }

        @Override
        public boolean isFirst() throws SQLException
        {
            return resultSet.isFirst();
        }

        @Override
        public boolean isLast() throws SQLException
        {
            return resultSet.isLast();
        }

        @Override
        public void beforeFirst() throws SQLException
        {
            resultSet.beforeFirst();
        }

        @Override
        public void afterLast() throws SQLException
        {
            resultSet.afterLast();
        }

        @Override
        public boolean first() throws SQLException
        {
            return resultSet.first();
        }

        @Override
        public boolean last() throws SQLException
        {
            return resultSet.last();
        }

        @Override
        public int getRow() throws SQLException
        {
            return resultSet.getRow();
        }

        @Override
        public boolean absolute(int row) throws SQLException
        {
            return resultSet.absolute(row);
        }

        @Override
        public boolean relative(int rows) throws SQLException
        {
            return resultSet.relative(rows);
        }

        @Override
        public boolean previous() throws SQLException
        {
            return resultSet.previous();
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException
        {
            resultSet.setFetchDirection(direction);
        }

        @Override
        public int getFetchDirection() throws SQLException
        {
            return resultSet.getFetchDirection();
        }

        @Override
        public void setFetchSize(int rows) throws SQLException
        {
            resultSet.setFetchSize(rows);
        }

        @Override
        public int getFetchSize() throws SQLException
        {
            return resultSet.getFetchSize();
        }

        @Override
        public int getType() throws SQLException
        {
            return resultSet.getType();
        }

        @Override
        public int getConcurrency() throws SQLException
        {
            return resultSet.getConcurrency();
        }

        @Override
        public boolean rowUpdated() throws SQLException
        {
            return resultSet.rowUpdated();
        }

        @Override
        public boolean rowInserted() throws SQLException
        {
            return resultSet.rowInserted();
        }

        @Override
        public boolean rowDeleted() throws SQLException
        {
            return resultSet.rowDeleted();
        }

        @Override
        public void updateNull(int columnIndex) throws SQLException
        {
            resultSet.updateNull(columnIndex);
        }

        @Override
        public void updateBoolean(int columnIndex, boolean x) throws SQLException
        {
            resultSet.updateBoolean(columnIndex, x);
        }

        @Override
        public void updateByte(int columnIndex, byte x) throws SQLException
        {
            resultSet.updateByte(columnIndex, x);
        }

        @Override
        public void updateShort(int columnIndex, short x) throws SQLException
        {
            resultSet.updateShort(columnIndex, x);
        }

        @Override
        public void updateInt(int columnIndex, int x) throws SQLException
        {
            resultSet.updateInt(columnIndex, x);
        }

        @Override
        public void updateLong(int columnIndex, long x) throws SQLException
        {
            resultSet.updateLong(columnIndex, x);
        }

        @Override
        public void updateFloat(int columnIndex, float x) throws SQLException
        {
            resultSet.updateFloat(columnIndex, x);
        }

        @Override
        public void updateDouble(int columnIndex, double x) throws SQLException
        {
            resultSet.updateDouble(columnIndex, x);
        }

        @Override
        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
        {
            resultSet.updateBigDecimal(columnIndex, x);
        }

        @Override
        public void updateString(int columnIndex, String x) throws SQLException
        {
            resultSet.updateString(columnIndex, x);
        }

        @Override
        public void updateBytes(int columnIndex, byte[] x) throws SQLException
        {
            resultSet.updateBytes(columnIndex, x);
        }

        @Override
        public void updateDate(int columnIndex, Date x) throws SQLException
        {
            resultSet.updateDate(columnIndex, x);
        }

        @Override
        public void updateTime(int columnIndex, Time x) throws SQLException
        {
            resultSet.updateTime(columnIndex, x);
        }

        @Override
        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
        {
            resultSet.updateTimestamp(columnIndex, x);
        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException
        {
            resultSet.updateAsciiStream(columnIndex, x, length);
        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException
        {
            resultSet.updateBinaryStream(columnIndex, x, length);
        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
        {
            resultSet.updateCharacterStream(columnIndex, x, length);
        }

        @Override
        public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException
        {
            resultSet.updateObject(columnIndex, x, scaleOrLength);
        }

        @Override
        public void updateObject(int columnIndex, Object x) throws SQLException
        {
            resultSet.updateObject(columnIndex, x);
        }

        @Override
        public void updateNull(String columnLabel) throws SQLException
        {
            resultSet.updateNull(columnLabel);
        }

        @Override
        public void updateBoolean(String columnLabel, boolean x) throws SQLException
        {
            resultSet.updateBoolean(columnLabel, x);
        }

        @Override
        public void updateByte(String columnLabel, byte x) throws SQLException
        {
            resultSet.updateByte(columnLabel, x);
        }

        @Override
        public void updateShort(String columnLabel, short x) throws SQLException
        {
            resultSet.updateShort(columnLabel, x);
        }

        @Override
        public void updateInt(String columnLabel, int x) throws SQLException
        {
            resultSet.updateInt(columnLabel, x);
        }

        @Override
        public void updateLong(String columnLabel, long x) throws SQLException
        {
            resultSet.updateLong(columnLabel, x);
        }

        @Override
        public void updateFloat(String columnLabel, float x) throws SQLException
        {
            resultSet.updateFloat(columnLabel, x);
        }

        @Override
        public void updateDouble(String columnLabel, double x) throws SQLException
        {
            resultSet.updateDouble(columnLabel, x);
        }

        @Override
        public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException
        {
            resultSet.updateBigDecimal(columnLabel, x);
        }

        @Override
        public void updateString(String columnLabel, String x) throws SQLException
        {
            resultSet.updateString(columnLabel, x);
        }

        @Override
        public void updateBytes(String columnLabel, byte[] x) throws SQLException
        {
            resultSet.updateBytes(columnLabel, x);
        }

        @Override
        public void updateDate(String columnLabel, Date x) throws SQLException
        {
            resultSet.updateDate(columnLabel, x);
        }

        @Override
        public void updateTime(String columnLabel, Time x) throws SQLException
        {
            resultSet.updateTime(columnLabel, x);
        }

        @Override
        public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException
        {
            resultSet.updateTimestamp(columnLabel, x);
        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException
        {
            resultSet.updateAsciiStream(columnLabel, x, length);
        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException
        {
            resultSet.updateBinaryStream(columnLabel, x, length);
        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException
        {
            resultSet.updateCharacterStream(columnLabel, reader, length);
        }

        @Override
        public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException
        {
            resultSet.updateObject(columnLabel, x, scaleOrLength);
        }

        @Override
        public void updateObject(String columnLabel, Object x) throws SQLException
        {
            resultSet.updateObject(columnLabel, x);
        }

        @Override
        public void insertRow() throws SQLException
        {
            resultSet.insertRow();
        }

        @Override
        public void updateRow() throws SQLException
        {
            resultSet.updateRow();
        }

        @Override
        public void deleteRow() throws SQLException
        {
            resultSet.deleteRow();
        }

        @Override
        public void refreshRow() throws SQLException
        {
            resultSet.refreshRow();
        }

        @Override
        public void cancelRowUpdates() throws SQLException
        {
            resultSet.cancelRowUpdates();
        }

        @Override
        public void moveToInsertRow() throws SQLException
        {
            resultSet.moveToInsertRow();
        }

        @Override
        public void moveToCurrentRow() throws SQLException
        {
            resultSet.moveToCurrentRow();
        }

        @Override
        public Statement getStatement() throws SQLException
        {
            return resultSet.getStatement();
        }

        @Override
        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException
        {
            return resultSet.getObject(columnIndex, map);
        }

        @Override
        public Ref getRef(int columnIndex) throws SQLException
        {
            return resultSet.getRef(columnIndex);
        }

        @Override
        public Blob getBlob(int columnIndex) throws SQLException
        {
            return resultSet.getBlob(columnIndex);
        }

        @Override
        public Clob getClob(int columnIndex) throws SQLException
        {
            return resultSet.getClob(columnIndex);
        }

        @Override
        public Array getArray(int columnIndex) throws SQLException
        {
            return resultSet.getArray(columnIndex);
        }

        @Override
        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException
        {
            return resultSet.getObject(columnLabel, map);
        }

        @Override
        public Ref getRef(String columnLabel) throws SQLException
        {
            return resultSet.getRef(columnLabel);
        }

        @Override
        public Blob getBlob(String columnLabel) throws SQLException
        {
            return resultSet.getBlob(columnLabel);
        }

        @Override
        public Clob getClob(String columnLabel) throws SQLException
        {
            return resultSet.getClob(columnLabel);
        }

        @Override
        public Array getArray(String columnLabel) throws SQLException
        {
            return resultSet.getArray(columnLabel);
        }

        @Override
        public Date getDate(int columnIndex, Calendar cal) throws SQLException
        {
            return resultSet.getDate(columnIndex, cal);
        }

        @Override
        public Date getDate(String columnLabel, Calendar cal) throws SQLException
        {
            return resultSet.getDate(columnLabel, cal);
        }

        @Override
        public Time getTime(int columnIndex, Calendar cal) throws SQLException
        {
            return resultSet.getTime(columnIndex, cal);
        }

        @Override
        public Time getTime(String columnLabel, Calendar cal) throws SQLException
        {
            return resultSet.getTime(columnLabel, cal);
        }

        @Override
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
        {
            return resultSet.getTimestamp(columnIndex, cal);
        }

        @Override
        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException
        {
            return resultSet.getTimestamp(columnLabel, cal);
        }

        @Override
        public URL getURL(int columnIndex) throws SQLException
        {
            return resultSet.getURL(columnIndex);
        }

        @Override
        public URL getURL(String columnLabel) throws SQLException
        {
            return resultSet.getURL(columnLabel);
        }

        @Override
        public void updateRef(int columnIndex, Ref x) throws SQLException
        {
            resultSet.updateRef(columnIndex, x);
        }

        @Override
        public void updateRef(String columnLabel, Ref x) throws SQLException
        {
            resultSet.updateRef(columnLabel, x);
        }

        @Override
        public void updateBlob(int columnIndex, Blob x) throws SQLException
        {
            resultSet.updateBlob(columnIndex, x);
        }

        @Override
        public void updateBlob(String columnLabel, Blob x) throws SQLException
        {
            resultSet.updateBlob(columnLabel, x);
        }

        @Override
        public void updateClob(int columnIndex, Clob x) throws SQLException
        {
            resultSet.updateClob(columnIndex, x);
        }

        @Override
        public void updateClob(String columnLabel, Clob x) throws SQLException
        {
            resultSet.updateClob(columnLabel, x);
        }

        @Override
        public void updateArray(int columnIndex, Array x) throws SQLException
        {
            resultSet.updateArray(columnIndex, x);
        }

        @Override
        public void updateArray(String columnLabel, Array x) throws SQLException
        {
            resultSet.updateArray(columnLabel, x);
        }

        @Override
        public RowId getRowId(int columnIndex) throws SQLException
        {
            return resultSet.getRowId(columnIndex);
        }

        @Override
        public RowId getRowId(String columnLabel) throws SQLException
        {
            return resultSet.getRowId(columnLabel);
        }

        @Override
        public void updateRowId(int columnIndex, RowId x) throws SQLException
        {
            resultSet.updateRowId(columnIndex, x);
        }

        @Override
        public void updateRowId(String columnLabel, RowId x) throws SQLException
        {
            resultSet.updateRowId(columnLabel, x);
        }

        @Override
        public int getHoldability() throws SQLException
        {
            return resultSet.getHoldability();
        }

        @Override
        public boolean isClosed() throws SQLException
        {
            return resultSet.isClosed();
        }

        @Override
        public void updateNString(int columnIndex, String nString) throws SQLException
        {
            resultSet.updateNString(columnIndex, nString);
        }

        @Override
        public void updateNString(String columnLabel, String nString) throws SQLException
        {
            resultSet.updateNString(columnLabel, nString);
        }

        @Override
        public void updateNClob(int columnIndex, NClob nClob) throws SQLException
        {
            resultSet.updateNClob(columnIndex, nClob);
        }

        @Override
        public void updateNClob(String columnLabel, NClob nClob) throws SQLException
        {
            resultSet.updateNClob(columnLabel, nClob);
        }

        @Override
        public NClob getNClob(int columnIndex) throws SQLException
        {
            return resultSet.getNClob(columnIndex);
        }

        @Override
        public NClob getNClob(String columnLabel) throws SQLException
        {
            return resultSet.getNClob(columnLabel);
        }

        @Override
        public SQLXML getSQLXML(int columnIndex) throws SQLException
        {
            return resultSet.getSQLXML(columnIndex);
        }

        @Override
        public SQLXML getSQLXML(String columnLabel) throws SQLException
        {
            return resultSet.getSQLXML(columnLabel);
        }

        @Override
        public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
        {
            resultSet.updateSQLXML(columnIndex, xmlObject);
        }

        @Override
        public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
        {
            resultSet.updateSQLXML(columnLabel, xmlObject);
        }

        @Override
        public String getNString(int columnIndex) throws SQLException
        {
            return resultSet.getNString(columnIndex);
        }

        @Override
        public String getNString(String columnLabel) throws SQLException
        {
            return resultSet.getNString(columnLabel);
        }

        @Override
        public Reader getNCharacterStream(int columnIndex) throws SQLException
        {
            return resultSet.getNCharacterStream(columnIndex);
        }

        @Override
        public Reader getNCharacterStream(String columnLabel) throws SQLException
        {
            return resultSet.getNCharacterStream(columnLabel);
        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException
        {
            resultSet.updateNCharacterStream(columnIndex, x, length);
        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
        {
            resultSet.updateNCharacterStream(columnLabel, reader, length);
        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException
        {
            resultSet.updateAsciiStream(columnIndex, x, length);
        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException
        {
            resultSet.updateBinaryStream(columnIndex, x, length);
        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException
        {
            resultSet.updateCharacterStream(columnIndex, x, length);
        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
        {
            resultSet.updateAsciiStream(columnLabel, x, length);
        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
        {
            resultSet.updateBinaryStream(columnLabel, x, length);
        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
        {
            resultSet.updateCharacterStream(columnLabel, reader, length);
        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
        {
            resultSet.updateBlob(columnIndex, inputStream, length);
        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
        {
            resultSet.updateBlob(columnLabel, inputStream, length);
        }

        @Override
        public void updateClob(int columnIndex, Reader reader, long length) throws SQLException
        {
            resultSet.updateClob(columnIndex, reader, length);
        }

        @Override
        public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
        {
            resultSet.updateClob(columnLabel, reader, length);
        }

        @Override
        public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
        {
            resultSet.updateNClob(columnIndex, reader, length);
        }

        @Override
        public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
        {
            resultSet.updateNClob(columnLabel, reader, length);
        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException
        {
            resultSet.updateNCharacterStream(columnIndex, x);
        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
        {
            resultSet.updateNCharacterStream(columnLabel, reader);
        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException
        {
            resultSet.updateAsciiStream(columnIndex, x);
        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException
        {
            resultSet.updateBinaryStream(columnIndex, x);
        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x) throws SQLException
        {
            resultSet.updateCharacterStream(columnIndex, x);
        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
        {
            resultSet.updateAsciiStream(columnLabel, x);
        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
        {
            resultSet.updateBinaryStream(columnLabel, x);
        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
        {
            resultSet.updateCharacterStream(columnLabel, reader);
        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
        {
            resultSet.updateBlob(columnIndex, inputStream);
        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
        {
            resultSet.updateBlob(columnLabel, inputStream);
        }

        @Override
        public void updateClob(int columnIndex, Reader reader) throws SQLException
        {
            resultSet.updateClob(columnIndex, reader);
        }

        @Override
        public void updateClob(String columnLabel, Reader reader) throws SQLException
        {
            resultSet.updateClob(columnLabel, reader);
        }

        @Override
        public void updateNClob(int columnIndex, Reader reader) throws SQLException
        {
            resultSet.updateNClob(columnIndex, reader);
        }

        @Override
        public void updateNClob(String columnLabel, Reader reader) throws SQLException
        {
            resultSet.updateNClob(columnLabel, reader);
        }

        @Override
        public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
        {
            return resultSet.getObject(columnIndex, type);
        }

        @Override
        public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
        {
            return resultSet.getObject(columnLabel, type);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException
        {
            return resultSet.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException
        {
            return resultSet.isWrapperFor(iface);
        }

        @Override
        public String toString()
        {
            return resultSet.toString();
        }

        @Override
        public int hashCode()
        {
            return resultSet.hashCode();
        }

        @Override
        public boolean equals(Object object)
        {
            return resultSet.equals(object);
        }

        protected final ResultSet resultSet;
    }
}
