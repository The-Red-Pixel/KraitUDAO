package com.theredpixelteam.kraitudao.annotations.expandable;

import java.lang.annotation.*;

@Repeatable(BuiltinExpandRule.BuiltinExpandRuleCollection.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltinExpandRule {
    Class<?> value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BuiltinExpandRuleCollection
    {
        BuiltinExpandRule[] value();
    }
}
