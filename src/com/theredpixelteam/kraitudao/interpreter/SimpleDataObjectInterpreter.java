package com.theredpixelteam.kraitudao.interpreter;

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
        UniqueDataObjectContainer(Class<?> type)
        {
            this.data = new HashMap<>();
            this.type = type;
        }

        void seal()
        {
            if(this.key == null)
                throw new DataObjectMalformationException("Key not defined");

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
        MultipleDataObjectContainer(Class<?> type)
        {
            this.type = type;
        }

        void seal()
        {
            if(this.primaryKey == null)
                throw new DataObjectMalformationException("Primary Key not defined");

            this.secondaryKeys = Collections.unmodifiableMap(secondaryKeys);
            this.values = Collections.unmodifiableMap(values);
        }

        @Override
        public ValueObject getPrimaryKey()
        {
            return primaryKey;
        }

        @Override
        public Map<String, ValueObject> getSecondaryKeys()
        {
            return secondaryKeys;
        }

        @Override
        public Optional<ValueObject> getValue(String name)
        {
            return Optional.ofNullable(values.get(name));
        }

        @Override
        public Map<String, ValueObject> getValues()
        {
            return values;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        final Class<?> type;

        ValueObject primaryKey;

        Map<String, ValueObject> secondaryKeys;

        Map<String, ValueObject> values;
    }
}
