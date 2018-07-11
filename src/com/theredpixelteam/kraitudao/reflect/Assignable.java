package com.theredpixelteam.kraitudao.reflect;

public interface Assignable {
    public Object get(Object object) throws Exception;

    public void set(Object object, Object value) throws Exception;

    public Class<?> getType();

    public boolean isStatic();
}
