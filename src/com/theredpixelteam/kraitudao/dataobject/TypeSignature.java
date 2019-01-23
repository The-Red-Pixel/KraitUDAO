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

import java.lang.reflect.*;
import java.util.*;

public final class TypeSignature {
    TypeSignature(Class<?> type,
                  String name,
                  int dimension,
                  TypeSignature[] upperBounds,
                  TypeSignature[] lowerBounds,
                  TypeSignature[] signatures)
    {
        this(null, type, name, dimension, upperBounds, lowerBounds, signatures);
    }

    TypeSignature(TypeSignature parent,
                  Class<?> type,
                  String name,
                  int dimension,
                  TypeSignature[] upperBounds,
                  TypeSignature[] lowerBounds,
                  TypeSignature[] signatures)
    {
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.dimension = dimension;
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
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

    public int getDimension()
    {
        return dimension;
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

    public boolean isArray()
    {
        return dimension != 0;
    }

    public TypeSignature[] getFurther()
    {
        return Arrays.copyOf(signatures, signatures.length);
    }

    public boolean hasFurther()
    {
        return signatures.length != 0;
    }

    public static TypeSignature of(Type type)
    {
        // TODO
        return null;
    }

    public static TypeSignature[] of(Type[] type)
    {
        TypeSignature[] arr = new TypeSignature[type.length];

        for (int i = 0; i < type.length; i++)
            arr[i] = of(type[i]);

        return arr;
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

        for (int i = 0; i < dimension; i++)
            buff.append("[]");

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
        return new TypeSignature(null, null, 0, EMPTY, EMPTY, null);
    }

    public static TypeSignature upperBoundedWildcard(Type... upperBounds)
    {
        return boundedWildcard(upperBounds, EMPTY_TYPES);
    }

    public static TypeSignature lowerBoundedWilcard(Type... lowerBounds)
    {
        return boundedWildcard(EMPTY_TYPES, lowerBounds);
    }

    public static TypeSignature boundedWildcard(Type[] upperBounds, Type[] lowerBounds)
    {
        return new TypeSignature(null, null, 0, of(upperBounds), of(lowerBounds), null);
    }

    public static TypeSignature variable(String name)
    {
        return new TypeSignature(null, name, 0, EMPTY, EMPTY, null);
    }

    public static TypeSignature boundedVariable(String name, Type... upperBounds)
    {
        return new TypeSignature(null, name, 0, of(upperBounds), EMPTY, null);
    }

    private final TypeSignature parent;

    private final String name;

    private final Class<?> type;

    private final int dimension;

    private final TypeSignature[] upperBounds;

    private final TypeSignature[] lowerBounds;

    private final TypeSignature[] signatures;

    private static final TypeSignature[] EMPTY = new TypeSignature[0];

    private static final Type[] EMPTY_TYPES = new Type[0];

    public static class Builder
    {
        Builder()
        {
            this(null);
        }

        Builder(Builder parent)
        {
            this.parent = parent;
        }

        public TypeSignature build()
        {
            return build(null);
        }

        TypeSignature build(TypeSignature parent)
        {
            if (type == null)
                if (!signatures.isEmpty())
                    throw new IllegalArgumentException("Wildcard or variable type cannot have further signatures");
                else if (dimension != 0)
                    throw new IllegalArgumentException("Certain component type required in Generic Array");

            TypeSignature[] uppers = new TypeSignature[upperBounds.size()];
            TypeSignature[] lowers = new TypeSignature[lowerBounds.size()];

            for (int i = 0; i < uppers.length; i++)
                uppers[i] = upperBounds.get(i).build();

            for (int i = 0; i < lowers.length; i++)
                lowers[i] = lowerBounds.get(i).build();

            TypeSignature[] s = new TypeSignature[signatures.size()];
            TypeSignature root = new TypeSignature(parent, type, name, dimension, uppers, lowers, s);

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

        public Builder dimension(int dimension)
        {
            this.dimension = dimension;
            return this;
        }

        public int dimension()
        {
            return dimension;
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

        public Builder appendRawExplicit(Class<?> type)
        {
            appendFurther().rawType(type);
            return this;
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

        public Builder appendArray(Class<?> componentType, int dimension)
        {
            return appendFurther().rawType(componentType).dimension(dimension);
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

        private int dimension = 0;

        private final ArrayList<Builder> upperBounds = new ArrayList<>();

        private final ArrayList<Builder> lowerBounds = new ArrayList<>();

        private final ArrayList<Builder> signatures = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception
    {
        Field f = TypeSignature.class.getDeclaredField("m");

        ParameterizedType t = (ParameterizedType) f.getGenericType();
        System.out.println(t.getRawType());

        TypeVariable<?> wildcardType = (TypeVariable<?>) t.getActualTypeArguments()[0];
        System.out.println(Arrays.asList(wildcardType.getBounds()));

        for (Type a : t.getActualTypeArguments())
            System.out.println(a + " (" + a.getClass().getCanonicalName() + ")");
    }

    private Map<?, Map<String, Integer>> m;
}
