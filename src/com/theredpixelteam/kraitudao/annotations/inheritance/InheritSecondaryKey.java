package com.theredpixelteam.kraitudao.annotations.inheritance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InheritSecondaryKey {
    public String field();

    public String name() default "";
}
