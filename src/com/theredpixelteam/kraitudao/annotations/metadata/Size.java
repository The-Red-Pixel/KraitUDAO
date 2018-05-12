package com.theredpixelteam.kraitudao.annotations.metadata;

import java.lang.annotation.*;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Repeatable(Size.RepeatableSize.class)
public @interface Size {
    @ExpandedName
    String name() default "";

    int n();

    @MetadataCollection(Size.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RepeatableSize {
        Size[] value();
    }
}
