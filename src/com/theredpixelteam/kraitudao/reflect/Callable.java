package com.theredpixelteam.kraitudao.reflect;

import com.theredpixelteam.redtea.util.Optional;

public interface Callable<T> {
    public T call(Object object, Object... arguments) throws Exception;

    public String getName();

    public Class<T> getReturnType();

    public Class<?>[] getArguments();

    public boolean isStatic();

    public default int getArgumentCount()
    {
        return getArguments().length;
    }

    @SuppressWarnings("unchecked")
    public default <R> Optional<Callable<R>> as(Class<T> returnType)
    {
        if(returnType.isAssignableFrom(getReturnType()))
            return Optional.of((Callable<R>) this);
        return Optional.empty();
    }
}
