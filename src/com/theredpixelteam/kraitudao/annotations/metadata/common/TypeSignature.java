package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;

import java.lang.annotation.*;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Repeatable(TypeSignature.RepeatableTypeSignature.class)
public @interface TypeSignature {
    @ExpandedName
    public String name() default "";

    public Class<?>[] signatured();

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @MetadataCollection(TypeSignature.class)
    public @interface RepeatableTypeSignature {
        TypeSignature[] value();
    }
}
