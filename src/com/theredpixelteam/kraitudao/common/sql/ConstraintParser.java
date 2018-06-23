package com.theredpixelteam.kraitudao.common.sql;

public interface ConstraintParser {
    public String parse(Constraint constraints, boolean onColumn);
}
