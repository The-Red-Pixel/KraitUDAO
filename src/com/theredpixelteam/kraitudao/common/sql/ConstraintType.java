package com.theredpixelteam.kraitudao.common.sql;

public class ConstraintType {
    public static final ConstraintType
            NOT_NULL = new ConstraintType(),
            UNIQUE = new ConstraintType(),
            PRIMARY_KEY = new ConstraintType(),
            FOREIGN_KEY = new ConstraintType(),
            CHECK = new ConstraintType(),
            DEFAULT = new ConstraintType(),
            AUTO_INCREMENT = new ConstraintType();
}
