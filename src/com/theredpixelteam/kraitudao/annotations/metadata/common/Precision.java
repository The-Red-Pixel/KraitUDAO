package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Repeatable(Precision.RepeatablePrecision.class)
public @interface Precision {
    @ExpandedName
    String name() default "";

    int p();

    int s();

    @MetadataCollection(Precision.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface RepeatablePrecision {
        Precision[] value();
    }
}
