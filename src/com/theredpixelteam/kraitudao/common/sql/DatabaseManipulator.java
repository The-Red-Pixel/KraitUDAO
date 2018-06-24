/*
 * DatabaseManipulator.java
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;

public interface DatabaseManipulator {
    public ResultSet query(Connection connection, String tableName, Pair<String, DataArgument>[] keys, String[] values)
            throws SQLException;

    public int delete(Connection connection, String tableName, Pair<String, DataArgument>[] keysAndValues)
            throws SQLException;

    public int insert(Connection connection, String tableName, Pair<String, DataArgument>[] values)
            throws SQLException;

    public void createTable(Connection connection, String tableName, Vector3<String, Class<?>, Constraint>[] columns, Constraint[] tableConstraints)
            throws SQLException;

    public boolean createTableIfNotExists(Connection connection, String tableName, Vector3<String, Class<?>, Constraint>[] columns, Constraint[] tableConstraints)
            throws SQLException;

    public void dropTable(Connection connection, String tableName)
            throws SQLException;

    public boolean dropTableIfExists(Connection connection, String tableName)
            throws SQLException;
}
