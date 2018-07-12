package com.theredpixelteam.kraitudao.reflect;

import com.theredpixelteam.redtea.util.Optional;

public interface Assignable<T> {
    public Object get(Object object) throws Exception;

    public void set(Object object, T value) throws Exception;

    public Class<T> getType();

    public boolean isStatic();

    @SuppressWarnings("unchecked")
    public default <R> Optional<Assignable<R>> as(Class<R> type)
    {
        if(type.isAssignableFrom(getType()))
            return Optional.of((Assignable<R>) this);
        return Optional.empty();
    }
}
