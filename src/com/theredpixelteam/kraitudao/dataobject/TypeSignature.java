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

        if (type != null)
            this.form = SignatureForm.EXPLICIT;
        else if (name == null)
            this.form = SignatureForm.WILDCARD;
        else
            this.form = SignatureForm.VARIABLE;
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
        return SignatureForm.EXPLICIT.equals(this.form);
    }

    public boolean isWildcard()
    {
        return SignatureForm.WILDCARD.equals(this.form);
    }

    public boolean isVariable()
    {
        return SignatureForm.VARIABLE.equals(this.form);
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

    public TypeSignature[] getUpperBounds()
    {
        return Arrays.copyOf(upperBounds, upperBounds.length);
    }

    public boolean hasUpperBounds()
    {
        return upperBounds.length != 0;
    }

    public TypeSignature[] getLowerBounds()
    {
        return Arrays.copyOf(lowerBounds, lowerBounds.length);
    }

    public boolean hasLowerBounds()
    {
        return lowerBounds.length != 0;
    }

    public boolean isCompatible(Class<?> type)
    {
        boolean flag = true;

        switch (this.form)
        {
            case EXPLICIT:
                return this.type.isAssignableFrom(type);

                // TODO
        }

        return flag;
    }

    public static TypeSignature of(Type type)
    {
        return inject(builder(), type).build();
    }

    private static Builder inject(Builder builder, Type type)
    {
        builder.dimension(getDimension(type));

        if (type instanceof Class<?>)
            builder.rawType((Class<?>) type);
        else if (type instanceof WildcardType)
        {
            WildcardType wildcard = (WildcardType) type;

            Type[] uppers = wildcard.getUpperBounds();
            Type[] lowers = wildcard.getLowerBounds();

            if (!(uppers.length == 1 && uppers[0].equals(Object.class)))
                builder.upperBounds(of(uppers));

            if (lowers.length != 0)
                builder.lowerBounds(of(lowers));
        }
        else if (type instanceof TypeVariable)
        {
            TypeVariable typevar = (TypeVariable) type;

            Type[] uppers = typevar.getBounds();

            if (!(uppers.length == 1 && uppers[0].equals(Object.class)))
                builder.upperBounds(of(uppers));

            builder.name(typevar.getName());
        }
        else if (type instanceof ParameterizedType)
        {
            ParameterizedType paramed = (ParameterizedType) type;

            builder.rawType((Class<?>) paramed.getRawType());

            for (Type param : paramed.getActualTypeArguments())
                inject(builder.appendFurther(), param);
        }

        return builder;
    }

    public static TypeSignature[] of(Type[] type)
    {
        TypeSignature[] arr = new TypeSignature[type.length];

        for (int i = 0; i < type.length; i++)
            arr[i] = of(type[i]);

        return arr;
    }

    public static TypeSignature format(TypeSignature format, Class<?>... params)
    {
        if (params.length == 0)
            return format;

        Builder root = builder();

        LinkedList<TypeSignature> typeSignatures = new LinkedList<>();
        LinkedList<Builder> builders = new LinkedList<>();

        typeSignatures.add(format);
        builders.add(root);

        int index = 0;

        while (!typeSignatures.isEmpty())
        {
            TypeSignature signature = typeSignatures.pollLast();
            Builder builder = builders.pollLast();

            if (signature.hasFurther())
            {
                TypeSignature[] further = signature.getFurther();
                Builder[] furtherBuilders = new Builder[further.length];

                for (int i = 0; i < further.length; i++)
                    furtherBuilders[i] = builder.appendFurther();

                for (int i = 0; i < further.length; i++)
                {
                    typeSignatures.add(further[further.length - i - 1]);
                    builders.add(furtherBuilders[further.length - i -  1]);
                }
            }
            else if (signature.isWildcard() || signature.isVariable())
            {
                if (index == params.length)
                {
                    builder
                            .name(signature.getName().getSilently())
                            .upperBounds(signature.getUpperBounds())
                            .lowerBounds(signature.getLowerBounds())
                            .dimension(signature.getDimension());

                    continue;
                }

                if (signature.isCompatible(params[index]))
                    builder.rawType(params[index++]);
                else
                    throw new IllegalArgumentException("Type: " + params[index - 1].getCanonicalName() + " is not compatible to signature: " + signature);

                continue;
            }

            builder
                    .rawType(signature.getRawType().orElseThrow(IllegalArgumentException::new))
                    .dimension(signature.getDimension());
        }

        return root.build();
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

    public static TypeSignature upperBoundedWildcard(TypeSignature... upperBounds)
    {
        return boundedWildcard(upperBounds, EMPTY);
    }

    public static TypeSignature lowerBoundedWilcard(TypeSignature... lowerBounds)
    {
        return boundedWildcard(EMPTY, lowerBounds);
    }

    public static TypeSignature boundedWildcard(TypeSignature[] upperBounds, TypeSignature[] lowerBounds)
    {
        return new TypeSignature(null, null, 0, upperBounds, lowerBounds, null);
    }

    public static TypeSignature variable(String name)
    {
        return new TypeSignature(null, name, 0, EMPTY, EMPTY, null);
    }

    public static TypeSignature boundedVariable(String name, TypeSignature... upperBounds)
    {
        return new TypeSignature(null, name, 0, upperBounds, EMPTY, null);
    }

    public static int getDimension(Class<?> type)
    {
        int dimension = 0;

        while ((type = type.getComponentType()) != null)
            dimension++;

        return dimension;
    }

    public static int getDimension(Type type)
    {
        int dimension = 0;

        while (type instanceof GenericArrayType)
        {
            type = ((GenericArrayType) type).getGenericComponentType();
            dimension++;
        }

        return dimension;
    }

    public static int compareDimension(Class<?> major, Class<?> minor)
    {
        return getDimension(major) - getDimension(minor);
    }

    public static int compareDimension(Type major, Type minor)
    {
        return getDimension(major) - getDimension(minor);
    }

    public static int compareDimension(Class<?> major, Type minor)
    {
        return getDimension(major) - getDimension(minor);
    }

    public static int compareDimension(Type major, Class<?> minor)
    {
        return getDimension(major) - getDimension(minor);
    }

    public static Class<?> getRootComponentType(Class<?> type)
    {
        Class<?> last = type;

        while ((type = type.getComponentType()) != null)
            last = type;

        return last;
    }

    public static Type getRootComponentType(Type type)
    {
        Type last = type;

        while (type instanceof GenericArrayType)
            type = ((GenericArrayType) type).getGenericComponentType();

        return type;
    }

    private final TypeSignature parent;

    private final String name;

    private final Class<?> type;

    private final int dimension;

    private final TypeSignature[] upperBounds;

    private final TypeSignature[] lowerBounds;

    private final TypeSignature[] signatures;

    private final SignatureForm form;

    private static final TypeSignature[] EMPTY = new TypeSignature[0];

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

            TypeSignature[] s = new TypeSignature[signatures.size()];
            TypeSignature root = new TypeSignature(parent, type, name, dimension,
                    upperBounds.toArray(new TypeSignature[0]),
                    lowerBounds.toArray(new TypeSignature[0]),
                    s);

            for (int i = 0; i < s.length; i++)
                s[i] = signatures.get(i).build(root);

            return root;
        }

        public Builder rawType(Class<?> type)
        {
            this.type = type;

            if (type != null)
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

        public Builder upperBounds(TypeSignature... upperBounds)
        {
            this.upperBounds.addAll(Arrays.asList(upperBounds));
            return this;
        }

        public List<TypeSignature> upperBounds()
        {
            return this.upperBounds;
        }

        public Builder lowerBounds(TypeSignature... lowerBounds)
        {
            this.lowerBounds.addAll(Arrays.asList(lowerBounds));
            return this;
        }

        public List<TypeSignature> lowerBounds()
        {
            return this.lowerBounds;
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

        private final ArrayList<TypeSignature> upperBounds = new ArrayList<>();

        private final ArrayList<TypeSignature> lowerBounds = new ArrayList<>();

        private final ArrayList<Builder> signatures = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception
    {
        Field f = TypeSignature.class.getDeclaredField("m");
        TypeSignature ts = of(f.getGenericType());

        System.out.println(ts);

        ts = TypeSignature.format(ts, String.class, Void.class);

        System.out.println(ts);
    }

    private Map<?, Map<?, Integer>> m;

    public static enum SignatureForm
    {
        EXPLICIT,
        WILDCARD,
        VARIABLE
    }
}
