package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Repeatable(NotNull.RepeatableNotNull.class)
public @interface NotNull {
    @ExpandedName
    public String name() default "";

    @MetadataCollection(NotNull.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface RepeatableNotNull
    {
        public NotNull[] value();
    }
}
