package com.theredpixelteam.kraitudao.common.sql;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DefaultDataTypeParser implements DataTypeParser {
    @Override
    public String parseType(DataType dataType)
    {
        return null;
    }

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

    public static final DefaultDataTypeParser INSTANCE = new DefaultDataTypeParser();
}
