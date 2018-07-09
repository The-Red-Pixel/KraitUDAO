package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;
import java.util.ArrayList;

@Metadata
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValueList.ValueListRepeatable.class)
public @interface ValueList {
    @ExpandedName
    public String name();

    public Class<?>[] signatured();

    public Class<?> type() default ArrayList.class;

    @MetadataCollection(ValueList.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValueListRepeatable {
        public ValueList[] value();
    }
}
