package com.theredpixelteam.kraitudao.annotations.expandable;

import java.lang.annotation.*;

@Repeatable(CustomExpandRule.CustomExpandRuleCollection.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomExpandRule {
    Class<?> type();

    Entry[] entries();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomExpandRuleCollection
    {
        CustomExpandRule[] value();
    }
}
