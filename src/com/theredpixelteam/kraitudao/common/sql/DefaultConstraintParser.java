package com.theredpixelteam.kraitudao.common.sql;

import com.theredpixelteam.kraitudao.DataSourceError;
import com.theredpixelteam.redtea.function.Function;

import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings("all")
public class DefaultConstraintParser implements ConstraintParser {
    @Override
    public String parse(Constraint constraint, boolean onColumn) /*throws DataSourceError*/
    {
        Map<ConstraintType, Function<Constraint, String>> map =
                onColumn ? ON_COLUM : ON_TABLE;

        Function<Constraint, String> parserFunction = map.get(constraint.getConstraint());

        if(parserFunction == null)
            throw new DataSourceError("unsupported constraint (on " + (onColumn ? "column" : "table") + ")");

        return parserFunction.apply(constraint);
    }

    private static String targets(String[] targets)
    {
        StringBuilder stringBuilder = new StringBuilder("(");

        int p = targets.length - 1, i = 0;
        while(i < p)
            stringBuilder.append(targets[i++]).append(",");
        stringBuilder.append(targets[i]);

        return stringBuilder.append(")").toString();
    }

    private static final Map<ConstraintType, Function<Constraint, String>> ON_COLUM =
            new IdentityHashMap<ConstraintType, Function<Constraint, String>>() {
                {
                    put(ConstraintType.NOT_NULL, constraint -> "NOT NULL");

                    put(ConstraintType.UNIQUE, constraint -> "UNIQUE");

                    put(ConstraintType.DEFAULT, constraint -> "DEFAULT " + constraint.getExpression()
                            .orElseThrow(() -> new DataSourceError("Empty expression of constraint \"DEFAULT\"")));

                    put(ConstraintType.AUTO_INCREMENT, constraint -> "AUTO_INCREMENT");
                }
            };

    private static final Map<ConstraintType, Function<Constraint, String>> ON_TABLE =
            new IdentityHashMap<ConstraintType, Function<Constraint, String>>() {
                {
                    put(ConstraintType.UNIQUE, constraint -> "UNIQUE " + targets(constraint.getTargets()
                            .orElseThrow(() -> new DataSourceError("Empty target of constraint \"UNIQUE\""))));

                    put(ConstraintType.PRIMARY_KEY, constraint -> "PRIMARY KEY " + targets(constraint.getTargets()
                            .orElseThrow(() -> new DataSourceError("Empty target of constraint \"PRIMARY_KEY\""))));

                    put(ConstraintType.FOREIGN_KEY, constraint -> "FOREIGN KEY (" + constraint.getTargets()
                            .orElseThrow(() -> new DataSourceError("Empty target of constraint \"FOREIGN_KEY\""))[0] + ") " +
                            "REFERENCES " + constraint.getReference()
                            .orElseThrow(() -> new DataSourceError("Empty reference of constraint \"FOREIGN_KEY\""))
                            .getReferencedTable() + "(" + constraint.getReference().get().getReferencedColumn() + ")");

                    put(ConstraintType.CHECK, constraint -> "CHECK (" + constraint.getExpression()
                            .orElseThrow(() -> new DataSourceError("Empty expression of constraint \"CHECK\"")) + ")");
                }
            };

    public static final DefaultConstraintParser INSTANCE = new DefaultConstraintParser();
}
