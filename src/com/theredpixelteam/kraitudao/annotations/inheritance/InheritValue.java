package com.theredpixelteam.kraitudao.annotations.inheritance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InheritValue {
    public String field();

    public String name() default "";

    public Class<?> source() default Void.class;

    public boolean strict() default false;
}
