package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Repeatable(IgnoreWhenEquals.RepeatableIgnoreWhenEquals.class)
public @interface IgnoreWhenEquals {
    @ExpandedName
    public String name() default "";

    public String value();

    @MetadataCollection(IgnoreWhenEquals.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface RepeatableIgnoreWhenEquals
    {
        IgnoreWhenEquals[] value();
    }
}
