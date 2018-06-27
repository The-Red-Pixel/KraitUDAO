package com.theredpixelteam.kraitudao.annotations.metadata.common;

import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Metadata
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExpandForcibly {
}
