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

import com.theredpixelteam.kraitudao.common.DataObjectCache;
import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.dataobject.DataObjectContainer;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;
import com.theredpixelteam.kraitudao.interpreter.StandardDataObjectInterpreter;
import com.theredpixelteam.redtea.util.Pair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseManipulator {
    public ResultSet query(Connection connection,
                           String tableName,
                           Pair<String, DataArgument>[] keys, String[] values)
            throws SQLException;

    public int delete(Connection connection,
                      String tableName,
                      Pair<String, DataArgument>[] keysAndValues)
            throws SQLException;

    public int insert(Connection connection,
                      String tableName,
                      Pair<String, DataArgument>[] values)
            throws SQLException;

    public void createTable(Connection connection,
                            String tableName,
                            DataObject dataObject)
            throws SQLException;

    public default void createTable(Connection connection,
                                    String tableName,
                                    Class<?> dataType,
                                    DataObjectContainer container,
                                    DataObjectInterpreter interpreter)
            throws SQLException, DataObjectInterpretationException
    {
        createTable(connection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    public default void createTable(Connection connection,
                                    String tableName,
                                    Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        createTable(connection, tableName, dataType, DataObjectCache.getGlobal(), StandardDataObjectInterpreter.INSTANCE);
    }

    public boolean createTableIfNotExists(Connection connection,
                                          String tableName,
                                          DataObject dataObject)
            throws SQLException;

    public default boolean createTableIfNotExists(Connection connection,
                                                  String tableName,
                                                  Class<?> dataType,
                                                  DataObjectContainer container,
                                                  DataObjectInterpreter interpreter)
            throws SQLException, DataObjectInterpretationException
    {
        return createTableIfNotExists(connection, tableName, container.interpretIfAbsent(dataType, interpreter));
    }

    public default boolean createTableIfNotExists(Connection connection,
                                                  String tableName,
                                                  Class<?> dataType)
            throws SQLException, DataObjectInterpretationException
    {
        return createTableIfNotExists(connection, tableName, dataType, DataObjectCache.getGlobal(), StandardDataObjectInterpreter.INSTANCE);
    }
}
