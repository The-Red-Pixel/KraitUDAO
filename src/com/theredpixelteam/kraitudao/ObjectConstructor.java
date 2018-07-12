package com.theredpixelteam.kraitudao;

import com.theredpixelteam.redtea.function.FunctionWithThrowable;
import com.theredpixelteam.redtea.util.Optional;

public interface ObjectConstructor<T> {
    public T newInstance(Object object) throws Exception;

    public Class<T> getType();

    @SuppressWarnings("unchecked")
    public default <R> Optional<ObjectConstructor<R>> as(Class<R> type)
    {
        if(type.isAssignableFrom(getType()))
            return Optional.of((ObjectConstructor<R>) this);
        return Optional.empty();
    }

    public static <T> ObjectConstructor<T> of(Class<T> type, FunctionWithThrowable<Object, T, Exception> supplier)
    {
        return new ObjectConstructor<T>() {
            @Override
            public T newInstance(Object object) throws Exception
            {
                return supplier.apply(object);
            }

            @Override
            public Class<T> getType()
            {
                return type;
            }
        };
    }
}
