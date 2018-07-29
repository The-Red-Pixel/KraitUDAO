package com.theredpixelteam.kraitudao.dataobject;

import com.theredpixelteam.kraitudao.ObjectConstructor;
import com.theredpixelteam.redtea.util.Optional;

public interface Constructable {
    public ObjectConstructor<?> getConstructor();

    @SuppressWarnings("unchecked")
    public default <T> Optional<ObjectConstructor<T>> getConstructor(Class<T> type)
    {
        ObjectConstructor<?> constructor = getConstructor();

        if(!type.isAssignableFrom(constructor.getType()))
            return Optional.empty();

        return Optional.of((ObjectConstructor<T>) constructor);
    }
}
