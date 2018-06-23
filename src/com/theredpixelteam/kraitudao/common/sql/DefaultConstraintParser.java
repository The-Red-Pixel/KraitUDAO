package com.theredpixelteam.kraitudao.common.sql;

public class DefaultConstraintParser implements ConstraintParser {
    @Override
    public String parse(Constraint constraint, boolean onColumn)
    {
        return null;
    }

    public static final DefaultConstraintParser INSTANCE = new DefaultConstraintParser();
}
