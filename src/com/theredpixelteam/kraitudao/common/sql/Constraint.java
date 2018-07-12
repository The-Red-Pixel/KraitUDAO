/*
 * Constraint.java
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

import com.theredpixelteam.redtea.util.Optional;

import java.util.Objects;

public class Constraint {
    protected Constraint(ConstraintType constraint, String[] targets, Reference reference, String expression)
    {
        this.constraint = Objects.requireNonNull(constraint, "constraint");
        this.targets = targets == null || targets.length == 0 ? null : targets;
        this.reference = reference;
        this.expression = expression;
    }

    public static Constraint ofNotNull()
    {
        return new Constraint(ConstraintType.NOT_NULL, null, null, null);
    }

    public static Constraint ofUnique()
    {
        return ofUnique(null);
    }

    public static Constraint ofUnique(String[] targets)
    {
        return new Constraint(ConstraintType.UNIQUE, targets, null, null);
    }

    public static Constraint ofPrimaryKey()
    {
        return ofPrimaryKey(null);
    }

    public static Constraint ofPrimaryKey(String[] targets)
    {
        return new Constraint(ConstraintType.PRIMARY_KEY, targets, null, null);
    }

    public static Constraint ofForeignKey(String target, String referencedTable, String referencedColumn)
    {
        return ofForeignKey(target, new Reference(referencedTable, referencedColumn));
    }

    public static Constraint ofForeignKey(String target, Reference reference)
    {
        return new Constraint(ConstraintType.FOREIGN_KEY, new String[] {target}, reference, null);
    }

    public static Constraint ofCheck(String expression)
    {
        return new Constraint(ConstraintType.CHECK, null, null, expression);
    }

    public static Constraint ofDefault(String expression)
    {
        return ofDefault(null, expression);
    }

    public static Constraint ofDefault(String target, String expression)
    {
        return new Constraint(ConstraintType.DEFAULT, target == null ? null : new String[] {target}, null, expression);
    }

    public static Constraint ofAutoIncrement()
    {
        return new Constraint(ConstraintType.AUTO_INCREMENT, null, null, null);
    }

    public ConstraintType getConstraint()
    {
        return this.constraint;
    }

    public Optional<String[]> getTargets()
    {
        return Optional.ofNullable(this.targets);
    }

    public Optional<Reference> getReference()
    {
        return Optional.ofNullable(this.reference);
    }

    public Optional<String> getExpression()
    {
        return Optional.ofNullable(this.expression);
    }

    private final ConstraintType constraint;

    private final String[] targets;

    private final Reference reference;

    private final String expression;

    public static class Reference
    {
        public Reference(String referencedTable, String referencedColumn)
        {
            this.referencedTable = referencedTable;
            this.referencedColumn = referencedColumn;
        }

        public static Reference of(String referencedTable, String referencedColumn)
        {
            return new Reference(referencedTable, referencedColumn);
        }

        public String getReferencedColumn()
        {
            return referencedColumn;
        }

        public String getReferencedTable()
        {
            return referencedTable;
        }

        private final String referencedTable;

        private final String referencedColumn;
    }
}
