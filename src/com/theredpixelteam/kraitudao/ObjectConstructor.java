package com.theredpixelteam.kraitudao;

import com.theredpixelteam.redtea.function.SupplierWithThrowable;

public interface ObjectConstructor<T> {
    public T newInstance() throws Exception;

    public Class<T> getType();

    public static <T> ObjectConstructor<T> of(Class<T> type, SupplierWithThrowable<T, Exception> supplier)
    {
        return new ObjectConstructor<T>() {
            @Override
            public T newInstance() throws Exception
            {
                return supplier.get();
            }

            @Override
            public Class<T> getType()
            {
                return type;
            }
        };
    }
}
