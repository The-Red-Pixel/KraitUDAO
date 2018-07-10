package com.theredpixelteam.kraitudao.annotations.location;

import com.theredpixelteam.kraitudao.PlaceHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldEntry {
    public FieldSource source();

    public String name();

    public Class<?> type() default PlaceHolder.class;
}
