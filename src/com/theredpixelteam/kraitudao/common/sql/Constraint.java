package com.theredpixelteam.kraitudao.common.sql;

import java.util.Objects;
import java.util.Optional;

public class Constraint {
    protected Constraint(ConstraintType constraint, String[] targets, Reference reference, String expression)
    {
        this.constraint = Objects.requireNonNull(constraint, "constraint");
        this.targets = targets == null || targets.length == 0 ? null : targets;
        this.reference = reference;
        this.expression = expression;
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
