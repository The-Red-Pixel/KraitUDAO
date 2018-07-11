package com.theredpixelteam.kraitudao.reflect;

public interface Assignable {
    public Object get(Object object);

    public boolean set(Object object, Object value);

    public Class<?> getType();

    public boolean isStatic();
}
