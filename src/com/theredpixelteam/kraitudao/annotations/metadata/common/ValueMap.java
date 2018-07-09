package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;
import java.util.HashMap;

@Metadata
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValueMap.ValueMapRepeatable.class)
public @interface ValueMap {
    @ExpandedName
    public String name();

    public Class<?>[] signatured();

    public Class<?> type() default HashMap.class;

    @MetadataCollection(ValueMap.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValueMapRepeatable {
        public ValueMap[] value();
    }
}
