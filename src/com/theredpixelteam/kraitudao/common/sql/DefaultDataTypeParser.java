package com.theredpixelteam.kraitudao.common.sql;

import com.theredpixelteam.kraitudao.DataSourceError;
import com.theredpixelteam.kraitudao.misc.TypeUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DefaultDataTypeParser implements DataTypeParser {
    @Override
    public String parseType(Class<?> dataType)
    {
        String type = MAPPING.get(TypeUtil.tryToUnbox(dataType));

        if(type == null)
            throw new DataSourceError("Unsupported type: " + dataType.getCanonicalName());

        return type;
    }

    private static final Map<Class<?>, String> MAPPING = new HashMap<Class<?>, String>() {
        {
            put(boolean.class,      "BIT");
            put(byte.class,         "TINYINT");
            put(char.class,         "NCHAR(1)");
            put(short.class,        "SMALLINT");
            put(int.class,          "INTEGER");
            put(long.class,         "BIGINT");
            put(float.class,        "REAL");
            put(double.class,       "DOUBLE");
            put(String.class,       "NVARCHAR");
            put(BigDecimal.class,   "DECIMAL");
        }
    };

    public static final DefaultDataTypeParser INSTANCE = new DefaultDataTypeParser();
}
