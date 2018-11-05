/*
 * TypeSignature.java
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

package com.theredpixelteam.kraitudao.dataobject;

import com.theredpixelteam.redtea.util.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TypeSignature {
    TypeSignature(Class<?> type, String name, TypeSignature[] signatures)
    {
        this(null, type, name, signatures);
    }

    TypeSignature(TypeSignature parent, Class<?> type, String name, TypeSignature[] signatures)
    {
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.signatures = signatures == null ? EMPTY : signatures;
    }

    public Optional<TypeSignature> getParent()
    {
        return Optional.ofNullable(parent);
    }

    public boolean hasParent()
    {
        return parent != null;
    }

    public Optional<Class<?>> getRawType()
    {
        return Optional.ofNullable(type);
    }

    public boolean hasRawType()
    {
        return type != null;
    }

    public Optional<String> getName()
    {
        return Optional.ofNullable(name);
    }

    public boolean hasName()
    {
        return name != null;
    }

    public boolean isExplicit()
    {
        return type != null;
    }

    public boolean isWildcard()
    {
        return type == null && name == null;
    }

    public boolean isVariable()
    {
        return type == null && name != null;
    }

    public TypeSignature[] getFurther()
    {
        return Arrays.copyOf(signatures, signatures.length);
    }

    public boolean hasFurther()
    {
        return signatures.length != 0;
    }

    @Override
    public String toString()
    {
        if (isVariable())
            return name;

        if (isWildcard())
            return "?";

        if (signatures.length == 0)
            return type.getCanonicalName();

        StringBuilder buff = new StringBuilder(type.getCanonicalName());

        buff.append("<");

        for (int i = 0; i < signatures.length - 1; i++)
            buff.append(signatures[i].toString()).append(", ");
        buff.append(signatures[signatures.length - 1].toString()).append(">");

        return buff.toString();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static Builder explicit(Class<?> rawType)
    {
        return new Builder().rawType(rawType);
    }

    public static TypeSignature wildcard()
    {
        return new TypeSignature(null, null, null);
    }

    public static TypeSignature variable(String name)
    {
        return new TypeSignature(null, name, null);
    }

    private final TypeSignature parent;

    private final String name;

    private final Class<?> type;

    private final TypeSignature[] signatures;

    private static final TypeSignature[] EMPTY = new TypeSignature[0];

    public static class Builder
    {
        Builder()
        {
            this(null);
        }

        Builder(Builder parent)
        {
            this.parent = null;
        }

        public TypeSignature build()
        {
            return build(null);
        }

        TypeSignature build(TypeSignature parent)
        {
            if (type == null && !signatures.isEmpty())
                throw new IllegalArgumentException("Wildcard or variable type cannot have further signatures");

            TypeSignature[] s = new TypeSignature[signatures.size()];
            TypeSignature root = new TypeSignature(parent, type, name, s);

            for (int i = 0; i < s.length; i++)
                s[i] = signatures.get(i).build(root);

            return root;
        }

        public Builder rawType(Class<?> type)
        {
            this.type = type;
            this.name = type.getCanonicalName();
            return this;
        }

        public Optional<Class<?>> rawType()
        {
            return Optional.ofNullable(type);
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Optional<String> name()
        {
            return Optional.ofNullable(name);
        }

        public Builder appendFurther()
        {
            Builder builder = new Builder(this);

            signatures.add(builder);

            return builder;
        }

        public Builder appendExplicit(Class<?> rawType)
        {
            return appendFurther().rawType(rawType);
        }

        public Builder appendVariable(String name)
        {
            appendFurther().name(name);
            return this;
        }

        public Builder appendWildcard()
        {
            appendFurther();
            return this;
        }

        public List<Builder> further()
        {
            return signatures;
        }

        public Optional<Builder> parent()
        {
            return Optional.ofNullable(parent);
        }

        public Builder escape()
        {
            return parent;
        }

        private final Builder parent;

        private Class<?> type;

        private String name;

        private final ArrayList<Builder> signatures = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception
    {
        Field f = TypeSignature.class.getDeclaredField("m");

        ParameterizedType t = (ParameterizedType) f.getGenericType();
        System.out.println(t.getRawType());

        for (Type a : t.getActualTypeArguments())
            System.out.println(a + " (" + a.getClass().getCanonicalName() + ")");
    }

    private Map<?, Map<String, Integer>> m;
}
