package com.theredpixelteam.kraitudao;

public interface ObjectConstructor<T> {
    public T newInstance() throws Exception;

    public Class<T> getType();
}
