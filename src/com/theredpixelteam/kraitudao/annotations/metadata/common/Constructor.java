package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.reflect.MethodEntry;
import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Constructor.ConstructorRepeatable.class)
public @interface Constructor {
    @ExpandedName
    public String name();

    public MethodEntry value();

    @MetadataCollection(Constructor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConstructorRepeatable {
        public Constructor[] value();
    }
}
