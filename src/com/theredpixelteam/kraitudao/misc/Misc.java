/*
 * Misc.java
 *
 * Copyright (C) 2018 The Red Pixel <theredpixelteam.com>
 * Copyright (C) 2018 KuCrO3 Studio <kucro3.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.theredpixelteam.kraitudao.misc;

import com.theredpixelteam.kraitudao.PlaceHolder;
import com.theredpixelteam.kraitudao.annotations.metadata.common.ValueList;
import com.theredpixelteam.kraitudao.annotations.metadata.common.ValueMap;
import com.theredpixelteam.kraitudao.annotations.metadata.common.ValueSet;
import com.theredpixelteam.kraitudao.dataobject.ValueObject;
import com.theredpixelteam.kraitudao.reflect.FieldEntry;
import com.theredpixelteam.kraitudao.reflect.MethodEntry;
import com.theredpixelteam.redtea.util.Optional;

import java.util.*;

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

    public static Optional<String> toString(Class<?> source, FieldEntry fieldEntry)
    {
        StringBuilder builder = new StringBuilder();
        switch(fieldEntry.source())
        {
            case OUTER_STATIC:
                if(fieldEntry.owner() == PlaceHolder.class)
                    return Optional.empty();
                source = fieldEntry.owner();

            case STATIC:
                builder.append("static ");

            case THIS:
                builder.append(source.getCanonicalName())
                        .append(".")
                        .append(fieldEntry.name());

                break;

            default:
                throw new Error("Should not reach here");
        }

        builder.append(":")
                .append(fieldEntry.type() == PlaceHolder.class ? "?" : fieldEntry.type().getCanonicalName());

        return Optional.of(builder.toString());
    }

    public static Optional<String> toString(Class<?> source, MethodEntry methodEntry)
    {
        StringBuilder builder = new StringBuilder();
        switch(methodEntry.source())
        {
            case STATIC:
                builder.append("static ");

            case THIS:
                builder.append(source.getCanonicalName());
                break;

            case OUTER_STATIC_FIELD:
                if(methodEntry.owner() == PlaceHolder.class)
                    return Optional.empty();
                source = methodEntry.owner();

            case STATIC_FIELD:
                builder.append("static ");

            case FIELD:
                if(methodEntry.field().isEmpty())
                    return Optional.empty();

                builder.append(source.getCanonicalName())
                        .append(".").append(methodEntry.field())
                        .append(".").append(methodEntry.method());
                break;

            case OUTER_STATIC:
                if(methodEntry.owner() == PlaceHolder.class)
                    return Optional.empty();

                builder.append("static ")
                        .append(methodEntry.owner().getCanonicalName())
                        .append(".").append(methodEntry.method());
                break;

            default:
                throw new Error("Should not reach here");
        }

        builder.append(toString(methodEntry.arguments()))
                .append(":")
                .append(methodEntry.returnType() == PlaceHolder.class ? "?" : methodEntry.returnType().getCanonicalName());

        return Optional.of(builder.toString());
    }

    private static String toString(Class<?>[] arguments)
    {
        if(arguments.length == 0)
            return "()";

        StringBuilder stringBuilder = new StringBuilder("(");
        for(int i = 0; i < arguments.length - 1; i++)
            stringBuilder.append(arguments[i].getCanonicalName()).append(",");
        stringBuilder.append(arguments[arguments.length - 1]).append(")");

        return stringBuilder.toString();
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
