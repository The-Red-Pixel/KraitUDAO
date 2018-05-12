package com.theredpixelteam.kraitudao.annotations.metadata;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface MetadataCollection {
    Class<?> value();
}
