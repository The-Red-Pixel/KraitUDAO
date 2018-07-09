package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;
import java.util.HashSet;

@Metadata
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValueSet.ValueSetRepeatable.class)
public @interface ValueSet {
    @ExpandedName
    public String name();

    public Class<?>[] signatured();

    public Class<?> type() default HashSet.class;

    @MetadataCollection(ValueSet.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValueSetRepeatable {
        public ValueSet[] value();
    }
}
