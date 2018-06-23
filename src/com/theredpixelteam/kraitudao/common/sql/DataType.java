package com.theredpixelteam.kraitudao.common.sql;

import java.util.Objects;

public class DataType {
    public DataType(Class<?> type)
    {
        this(type, false, false, false);
    }

    public DataType(Class<?> type, int integerPrecision, int decimalPrecision)
    {
        this(type, true, false, false);
        this.strings = new String[] {String.valueOf(integerPrecision), String.valueOf(decimalPrecision)};
    }

    public DataType(Class<?> type, int capacity)
    {
        this(type, false, true, false);
        this.strings = new String[] {String.valueOf(capacity)};
    }

    public DataType(Class<?> type, String[] expressions)
    {
        this(type, false, false, true);
        this.strings = Objects.requireNonNull(expressions);
    }

    private DataType(Class<?> type, boolean precisionDeclared, boolean capacityDeclared, boolean expressionsDeclared)
    {
        this.type = type;
        this.precisionDeclared = precisionDeclared;
        this.capacityDeclared = capacityDeclared;
        this.expressionsDeclared = expressionsDeclared;
    }

    public Class<?> getType()
    {
        return type;
    }

    public boolean isPrecisionDeclared()
    {
        return precisionDeclared;
    }

    public boolean isCapacityDeclared()
    {
        return capacityDeclared;
    }

    public boolean isExpressionsDeclared()
    {
        return expressionsDeclared;
    }

    public String[] getStrings()
    {
        return strings;
    }

    private final Class<?> type;

    private String[] strings;

    private final boolean precisionDeclared;

    private final boolean capacityDeclared;

    private final boolean expressionsDeclared;
}
