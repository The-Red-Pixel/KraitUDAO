/*
 * Test.java
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

package com.theredpixelteam.kraitudao;

import com.theredpixelteam.kraitudao.annotations.*;
import com.theredpixelteam.kraitudao.annotations.metadata.common.NotNull;
import com.theredpixelteam.kraitudao.annotations.metadata.common.Precision;
import com.theredpixelteam.kraitudao.common.DataObjectCache;
import com.theredpixelteam.kraitudao.common.PlainSQLDatabaseDataSource;
import com.theredpixelteam.kraitudao.common.sql.DefaultDatabaseManipulator;
import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.dataobject.MultipleDataObject;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;
import com.theredpixelteam.kraitudao.interpreter.StandardDataObjectInterpreter;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Collection;

public class Test {
    public static void main(String[] args) throws Exception
    {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/E:\\database\\test");

        DataObjectInterpreter interpreter = new StandardDataObjectInterpreter();
        DataObject dataObject = interpreter.get(A.class);

        DefaultDatabaseManipulator.INSTANCE.createTableIfNotExists(connection, "TESTB", B.class,
                DataObjectCache.getGlobal(), StandardDataObjectInterpreter.INSTANCE);

        B b = new B();
        DataSource dataSource = new PlainSQLDatabaseDataSource(connection, "TESTB");

        System.out.println(((MultipleDataObject)DataObjectCache.getGlobal().get(B.class).get()).getSecondaryKeys());

        for(B ab : dataSource.pullVaguely(b))
            System.out.println(ab.B);
    }

    @Unique
    public static class A
    {
        @Key
        public int a;

        @NotNull
        @Value
        public int b;
    }

    @Multiple
    public static class B
    {
        @PrimaryKey
        public int A = 0;

        @SecondaryKey
        public int B;

        @Value
        public int C;
    }

}
