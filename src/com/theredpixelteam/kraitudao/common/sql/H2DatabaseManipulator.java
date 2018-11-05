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

import com.theredpixelteam.redtea.util.Pair;
import com.theredpixelteam.redtea.util.Vector3;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class H2DatabaseManipulator implements DatabaseManipulator {
    public H2DatabaseManipulator()
    {
        this(DefaultDataTypeParser.INSTANCE, DefaultConstraintParser.INSTANCE);
    }

    public H2DatabaseManipulator(DataTypeParser dataTypeParser)
    {
        this(dataTypeParser, DefaultConstraintParser.INSTANCE);
    }

    public H2DatabaseManipulator(ConstraintParser constraintParser)
    {
        this(DefaultDataTypeParser.INSTANCE, constraintParser);
    }

    public H2DatabaseManipulator(DataTypeParser dataTypeParser, ConstraintParser constraintParser)
    {
        setDataTypeParser(dataTypeParser);
        setConstraintParser(constraintParser);
    }

    @Override
    public ResultSet query(Connection connection, String tableName, Pair<String, DataArgument>[] keys, String[] values)
            throws SQLException
    {
        return query0(connection, tableName, keys, values, false, 0);
    }

    @Override
    public ResultSet queryTop(Connection connection, String tableName, Pair<String, DataArgument>[] keys, String[] values, int limit)
            throws SQLException
    {
        return query0(connection, tableName, keys, values, true, limit);
    }

    private ResultSet query0(Connection connection, String tableName, Pair<String, DataArgument>[] keys, String[] values, boolean top, int limit)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT " + (top ? ("TOP " + limit + " ") : "") + combine(values, ",", "*") +
                        " FROM " + tableName + " WHERE " + narrow(keys)
        );

        injectArguments(preparedStatement, keys);

        return new ResultSetFromDisposableStatement(preparedStatement.executeQuery());
    }

    @Override
    public Collection<String> queryTables(Connection connection, String pattern) throws SQLException
    {
        Collection<String> tables = new ArrayList<>();

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE SQL IS NOT NULL"
                + (pattern == null ? "" : "AND TABLE_NAME LIKE '" + pattern + "'")
        );

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next())
                tables.add(resultSet.getString("TABLE_NAME"));
        }

        return tables;
    }

    @Override
    public int delete(Connection connection, String tableName, Pair<String, DataArgument>[] keysAndValues)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM " + tableName +
                        " WHERE " + narrow(keysAndValues)
        );

        injectArguments(preparedStatement, keysAndValues);

        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n;
    }

    @Override
    public int insert(Connection connection, String tableName, Pair<String, DataArgument>[] values) throws SQLException
    {
        if(values == null || values.length == 0)
            return 0;

        PreparedStatement preparedStatement = connection.prepareStatement(
                "MERGE INTO " + tableName +
                        " (" + combine(values, ",", null) + ")" +
                        " VALUES (" + arguments(values.length) + ")"
        );

        injectArguments(preparedStatement, values);

        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n;
    }

    @Override
    public void createTable(Connection connection, String tableName, Vector3<String, Class<?>, Constraint[]>[] columns, Constraint[] tableConstraints)
            throws SQLException
    {
        createTable0(connection, tableName, columns, tableConstraints, false);
    }

    @Override
    public boolean createTableIfNotExists(Connection connection, String tableName, Vector3<String, Class<?>, Constraint[]>[] columns, Constraint[] tableConstraints)
            throws SQLException
    {
        return createTable0(connection, tableName, columns, tableConstraints, true);
    }

    @Override
    public void cleanTable(Connection connection, String... tableNames) throws SQLException
    {
        if (tableNames.length == 0)
            return;

        Statement statement = connection.createStatement();

        for(int i = 0; i < tableNames.length; i++)
            statement.addBatch("DELETE FROM " + tableNames[i]);

        statement.executeBatch();

        statement.clearBatch();
        statement.close();
    }

    private boolean createTable0(Connection connection, String tableName, Vector3<String, Class<?>, Constraint[]>[] columns, Constraint[] tableConstraints, boolean onNotExists)
            throws SQLException
    {
        if(columns.length == 0)
            throw new SQLException("Creating a table with no columns");

        PreparedStatement preparedStatement = connection.prepareStatement(
                "CREATE TABLE " + (onNotExists ? "IF NOT EXISTS " : "") + tableName +
                        " (" + columnsNConstraints(columns, tableConstraints, tableName) + ")"
        );

        int n = preparedStatement.executeUpdate();

        preparedStatement.close();

        return n != 0;
    }

    @Override
    public void dropTable(Connection connection, String... tableName) throws SQLException
    {
        dropTable0(connection, tableName, false);
    }

    @Override
    public boolean dropTableIfExists(Connection connection, String... tableName) throws SQLException
    {
        return dropTable0(connection, tableName, true);
    }

    @Override
    public boolean supportType(Class<?> type)
    {
        return dataTypeParser.supportType(type);
    }

    private boolean dropTable0(Connection connection, String[] tableNames, boolean onExists) throws SQLException
    {
        if (tableNames.length == 0)
            return false;

        Statement statement = connection.createStatement();

        for (int i = 0; i < tableNames.length; i++)
            statement.addBatch(
                    "DROP TABLE " + (onExists ? "IF EXISTS " : "") + tableNames[i]
            );

        int[] n = statement.executeBatch();

        statement.clearBatch();
        statement.close();

        for (int nx : n)
            if (nx != 0)
                return true;

        return false;
    }

    public ConstraintParser getConstraintParser()
    {
        return this.constraintParser;
    }

    public void setConstraintParser(ConstraintParser parser)
    {
        this.constraintParser = Objects.requireNonNull(parser);
    }

    public DataTypeParser getDataTypeParser()
    {
        return dataTypeParser;
    }

    public void setDataTypeParser(DataTypeParser parser)
    {
        this.dataTypeParser = Objects.requireNonNull(parser);
    }

    protected String columnsNConstraints(Vector3<String, Class<?>, Constraint[]>[] columns, Constraint[] tableConstraints, String tableName)
    {
        StringBuilder statement = new StringBuilder();

        statement.append(columns(columns));

        if(tableConstraints.length > 0)
            statement.append(",").append(constraints(tableConstraints, tableName));

        return statement.toString();
    }

    protected String columns(Vector3<String, Class<?>, Constraint[]>[] columns)
    {
        StringBuilder statement = new StringBuilder();

        int i = 0;
        while (true)
        {
            statement
                    .append(columns[i].first()).append(" ")
                    .append(dataTypeParser.parseType(columns[i].second())).append(" ");

            for (Constraint constraint : columns[i].third())
                statement.append(constraintParser.parse(constraint, true)).append(" ");

            if (++i < columns.length)
                statement.append(",");
            else
                break;
        }

        return statement.toString();
    }

    protected String constraints(Constraint[] tableConstraints, String tableName)
    {
        StringBuilder statement = new StringBuilder();

        int p = tableConstraints.length - 1, i = 0;
        while(i < p)
            parseConstraint(statement, tableConstraints[i], tableName, i++);
        parseConstraint(statement, tableConstraints[i], tableName, i);

        return statement.toString();
    }

    protected void parseConstraint(StringBuilder statement, Constraint tableConstraint, String tableName, int i)
    {
        statement
                .append("CONSTRAINT CONSTRAINT_XXSYNTHETIC_").append(tableName).append("_").append(i).append(" ")
                .append(constraintParser.parse(tableConstraint, false))
                .append(",");
    }

    protected static void injectArguments(PreparedStatement preparedStatement, Pair<String, DataArgument>[] arguments)
            throws SQLException
    {
        if (arguments == null || arguments.length == 0)
            return;

        for(int i = 0; i < arguments.length;)
            arguments[i].second().apply(preparedStatement, ++i);
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

    private ConstraintParser constraintParser;

    private DataTypeParser dataTypeParser;

    public static final DatabaseManipulator INSTANCE = new H2DatabaseManipulator();
}
