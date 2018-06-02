package com.theredpixelteam.kraitudao.dataobject;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

public interface Metadatable {
    public <T extends Annotation> Optional<T> getMetadata(Class<T> type);

    public default <T extends Annotation> boolean hasMetadata(Class<T> type)
    {
        return getMetadata(type).isPresent();
    }

    public Map<Class<?>, Annotation> getMetadataMap();
}
