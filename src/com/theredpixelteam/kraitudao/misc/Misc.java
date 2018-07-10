package com.theredpixelteam.kraitudao.misc;

import com.theredpixelteam.kraitudao.annotations.metadata.common.ValueList;
import com.theredpixelteam.kraitudao.annotations.metadata.common.ValueMap;
import com.theredpixelteam.kraitudao.annotations.metadata.common.ValueSet;
import com.theredpixelteam.kraitudao.dataobject.ValueObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Misc {
    private Misc()
    {
    }

    public static boolean checkType(ValueMap metadata, Class<?> fieldType)
    {
        return Map.class.isAssignableFrom(metadata.type())
                && (fieldType == null || fieldType.isAssignableFrom(metadata.type()));
    }

    public static boolean checkType(ValueList metadata, Class<?> fieldType)
    {
        return List.class.isAssignableFrom(metadata.type())
                && (fieldType == null || fieldType.isAssignableFrom(metadata.type()));
    }

    public static boolean checkType(ValueSet metadata, Class<?> fieldType)
    {
        return Set.class.isAssignableFrom(metadata.type())
                && (fieldType == null || fieldType.isAssignableFrom(metadata.type()));
    }

    public static <X extends Throwable> void construct(ValueMap metadata, ValueObject valueObject) throws X
    {

    }

    public static Class<?> tryToUnbox(Class<?> type)
    {
        Class<?> unboxed = BOXING.get(type);

        return unboxed == null ? type : unboxed;
    }

    private static final Map<Class<?>, Class<?>> BOXING = new HashMap<Class<?>, Class<?>>() {
        {
            //  Boxed type       |  Unboxed type
            put(Boolean.class,      boolean.class);
            put(Character.class,    char.class);
            put(Short.class,        short.class);
            put(Integer.class,      int.class);
            put(Long.class,         long.class);
            put(Float.class,        float.class);
            put(Double.class,       double.class);
        }
    };
}
