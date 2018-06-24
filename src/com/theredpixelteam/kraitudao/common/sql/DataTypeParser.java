package com.theredpixelteam.kraitudao.common.sql;

public interface DataTypeParser {
    String parseType(Class<?> dataType);

    boolean supportType(Class<?> dataType);
}
