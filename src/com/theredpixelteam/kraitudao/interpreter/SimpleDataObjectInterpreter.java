package com.theredpixelteam.kraitudao.interpreter;

import com.sun.org.apache.xpath.internal.operations.Mult;
import com.theredpixelteam.kraitudao.dataobject.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class SimpleDataObjectInterpreter implements DataObjectInterpreter {
    @Override
    public DataObject get(Class<?> type) throws DataObjectInterpretationException
    {
        return null;
    }

    @Override
    public MultipleDataObject getMultiple(Class<?> type) throws DataObjectInterpretationException
    {
        return null;
    }

    @Override
    public UniqueDataObject getUnique(Class<?> type) throws DataObjectInterpretationException
    {
        return null;
    }

    private static class UniqueDataObjectContainer implements UniqueDataObject
    {
        UniqueDataObjectContainer(Class<?> type, ValueObject key)
        {
            this.data = new HashMap<>();
            this.type = type;
            this.key = key;
        }

        void seal()
        {
            this.data = Collections.unmodifiableMap(data);
        }

        @Override
        public ValueObject getKey()
        {
            return key;
        }

        @Override
        public Optional<ValueObject> getValue(String name)
        {
            return Optional.ofNullable(data.get(name));
        }

        @Override
        public Map<String, ValueObject> getValues()
        {
            return data;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        final Class<?> type;

        ValueObject key;

        Map<String, ValueObject> data;
    }

    private static class MultipleDataObjectContainer implements MultipleDataObject
    {
        MultipleDataObjectContainer()
        {

        }

        @Override
        public ValueObject getPrimaryKey()
        {
            return null;
        }

        @Override
        public Map<String, ValueObject> getSecondaryKeys()
        {
            return null;
        }

        @Override
        public Optional<ValueObject> getValue(String name)
        {
            return Optional.empty();
        }

        @Override
        public Map<String, ValueObject> getValues()
        {
            return null;
        }

        @Override
        public Class<?> getType()
        {
            return null;
        }

        final Class<?> type;


    }
}
