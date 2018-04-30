package com.theredpixelteam.kraitudao.annotations.inheritance;

import com.theredpixelteam.kraitudao.PlaceHolder;
import com.theredpixelteam.kraitudao.annotations.expandable.Expandable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InheritValue {
    public String field();

    public String name() default "";

    public Class<?> source() default PlaceHolder.class;

    public Class<?> type() default PlaceHolder.class;

    public boolean strict() default false;

    public Expandable expanding() default @Expandable(entries = {});
}
