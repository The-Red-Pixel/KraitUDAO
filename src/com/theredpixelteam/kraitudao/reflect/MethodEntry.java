package com.theredpixelteam.kraitudao.reflect;

import com.theredpixelteam.kraitudao.PlaceHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MethodEntry {
    public MethodSource source();

    public Class<?> owner() default PlaceHolder.class;

    public Class<?> returnType() default PlaceHolder.class;

    public Class<?> fieldType() default PlaceHolder.class;

    public String field() default "";

    public String method();

    public Class<?>[] arguments() default {};
}
