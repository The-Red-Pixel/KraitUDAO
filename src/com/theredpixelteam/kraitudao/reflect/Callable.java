package com.theredpixelteam.kraitudao.reflect;

public interface Callable {
    public Object call(Object object, Object... arguments) throws Exception;

    public String getName();

    public Class<?> getReturnType();

    public Class<?>[] getArguments();

    public boolean isStatic();

    public default int getArgumentCount()
    {
        return getArguments().length;
    }
}
