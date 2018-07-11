package com.theredpixelteam.kraitudao.misc;

import com.theredpixelteam.kraitudao.PlaceHolder;
import com.theredpixelteam.kraitudao.reflect.*;
import com.theredpixelteam.redtea.function.FunctionWithThrowable;
import com.theredpixelteam.redtea.util.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {
    private Reflection()
    {
    }

    public static Optional<Callable> of(Class<?> source, MethodEntry metadata)
    {
        Field field = null;
        Class<?> methodSource = null;
        FunctionWithThrowable<Object, Object, Exception> objectGetter = null;

        boolean requireStatic = false, requireStaticField = false;

        switch(metadata.source())
        {
            case STATIC_FIELD: // static
                requireStaticField = true;
            case FIELD:
                try {
                    field = source.getDeclaredField(metadata.field());
                } catch (NoSuchFieldException e) {
                    return Optional.empty();
                }

            case OUTER_STATIC_FIELD: // static
                if (field == null)
                {
                    Class<?> outer = metadata.owner();

                    if(outer == PlaceHolder.class)
                        throw new IllegalArgumentException("\"owner\" not declared in @MethodEntry of OUTER_STATIC_FIELD");

                    try {
                        field = outer.getDeclaredField(metadata.field());
                    } catch (NoSuchFieldException e) {
                        return Optional.empty();
                    }

                    requireStaticField = true;
                }

                if (Modifier.isStatic(field.getModifiers()) != requireStaticField)
                    return Optional.empty();

                if (metadata.fieldType() != PlaceHolder.class && metadata.fieldType() != field.getType())
                    return Optional.empty();

                final Field _field = field;
                objectGetter = (object) -> _field.get(object);
                methodSource = _field.getType();

            case OUTER_STATIC: // static
                if (field == null)
                {
                    Class<?> outer = metadata.owner();

                    if(outer == PlaceHolder.class)
                        throw new IllegalArgumentException("\"owner\" not declared in @MethodEntry of OUTER_STATIC");

                    objectGetter = (object) -> null;
                    methodSource = outer;
                    requireStatic = true;
                }

            case STATIC: // static
                if (methodSource == null)
                    requireStatic = true;

            case THIS:
                if (methodSource == null)
                {
                    methodSource = source;
                    objectGetter = (object) -> object;
                }

                try {
                    Method method = methodSource.getDeclaredMethod(metadata.method(), metadata.arguments());

                    if(Modifier.isStatic(method.getModifiers()) != requireStatic)
                        return Optional.empty();

                    Class<?> returnType = metadata.returnType();

                    if (returnType != PlaceHolder.class && returnType != method.getReturnType())
                        return Optional.empty();

                    return Optional.of(new CallableImpl(
                            method,
                            objectGetter,
                            !(metadata.source().equals(MethodSource.THIS) || metadata.source().equals(MethodSource.FIELD))
                    ));
                } catch (NoSuchMethodException e) {
                    return Optional.empty();
                }

            default:
                throw new Error("Should not reach here");
        }
    }

    public static Optional<Assignable> of(Class<?> source, FieldEntry metadata)
    {
        boolean requireStatic = false;

        switch(metadata.source())
        {
            case STATIC:
                requireStatic = true;
            case THIS:
                try {
                    Field field = source.getDeclaredField(metadata.name());

                    if(metadata.type() != PlaceHolder.class && metadata.type() != field.getType())
                        return Optional.empty();

                    if(Modifier.isStatic(field.getModifiers()) != requireStatic)
                        return Optional.empty();

                    return Optional.of(new AssignableImpl(field));
                } catch (NoSuchFieldException e) {
                    return Optional.empty();
                }

            case OUTER_STATIC:
                try {
                    Class<?> owner = metadata.owner();

                    if(owner == PlaceHolder.class)
                        throw new IllegalArgumentException("\"owner\" not declared in @FieldEntry of OUTER_STATIC");

                    Field field = owner.getDeclaredField(metadata.name());

                    if(metadata.type() != PlaceHolder.class && metadata.type() != field.getType())
                        return Optional.empty();

                    if(!Modifier.isStatic(field.getModifiers()))
                        return Optional.empty();

                    return Optional.of(new AssignableImpl(field));
                } catch (NoSuchFieldException e) {
                    return Optional.empty();
                }

            default:
                throw new Error("Should not reach here");
        }
    }

    private static class AssignableImpl implements Assignable
    {
        private AssignableImpl(Field field)
        {
            (this.field = field).setAccessible(true);
        }

        @Override
        public Object get(Object object) throws Exception
        {
            return field.get(object);
        }

        @Override
        public void set(Object object, Object value) throws Exception
        {
            field.set(object, value);
        }

        @Override
        public Class<?> getType()
        {
            return field.getType();
        }

        @Override
        public boolean isStatic()
        {
            return Modifier.isStatic(field.getModifiers());
        }

        private final Field field;
    }

    private static class CallableImpl implements Callable
    {
        private CallableImpl(Method method, FunctionWithThrowable<Object, Object, Exception> objectGetter, boolean isStatic)
        {
            (this.method = method).setAccessible(true);
            this.objectGetter = objectGetter;
            this.isStatic = isStatic;
        }

        @Override
        public Object call(Object object, Object... arguments) throws Exception
        {
            return method.invoke(objectGetter.apply(object), arguments);
        }

        @Override
        public String getName()
        {
            return method.getName();
        }

        @Override
        public Class<?> getReturnType()
        {
            return method.getReturnType();
        }

        @Override
        public Class<?>[] getArguments()
        {
            return method.getParameterTypes();
        }

        @Override
        public boolean isStatic()
        {
            return isStatic;
        }

        private final boolean isStatic;

        private final Method method;

        private final FunctionWithThrowable<Object, Object, Exception> objectGetter;
    }
}
