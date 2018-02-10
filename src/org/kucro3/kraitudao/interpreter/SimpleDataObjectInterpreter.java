package org.kucro3.kraitudao.interpreter;

import org.kucro3.kraitudao.annotations.Multiple;
import org.kucro3.kraitudao.annotations.Unique;
import org.kucro3.kraitudao.dataobject.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class SimpleDataObjectInterpreter implements DataObjectInterpreter {
    @Override
    public DataObject get(Class<?> type) throws DataObjectInterpretationException
    {
        boolean isMultiple = isMultiple(type);
        boolean isUnique = isUnique(type);

        if(isMultiple && isUnique)
            throw DuplicatedMetadata();

        if(isMultiple)
            return getMultiple0(type);
        else if(isUnique)
            return getUnique0(type);
        else
            throw MetadataNotFound();
    }

    @Override
    public MultipleDataObject getMultiple(Class<?> type) throws DataObjectInterpretationException
    {
        if(!isMultiple(type))
            throw MetadataNotFound();
        return getMultiple0(type);
    }

    @Override
    public UniqueDataObject getUnique(Class<?> type) throws DataObjectInterpretationException
    {
        if(!isUnique(type))
            throw MetadataNotFound();
        return getUnique0(type);
    }

    private MultipleDataObject getMultiple0(Class<?> type)
    {
        return null;
    }

    private UniqueDataObject getUnique0(Class<?> type)
    {
        return null;
    }

    private boolean isMultiple(Class<?> type)
    {
        return type.getAnnotation(Multiple.class) != null;
    }

    private boolean isUnique(Class<?> type)
    {
        return type.getAnnotation(Unique.class) != null;
    }

    private static DataObjectInterpretationException MetadataNotFound()
    {
        return new DataObjectInterpretationException("Metadata not found");
    }

    private static DataObjectInterpretationException DuplicatedMetadata()
    {
        return new DataObjectInterpretationException("Duplicated metadata");
    }

    private final Map<Class<?>, DataObject> cached = new HashMap<>();

    private static class SimpleDataObjectBuilder
    {
        private SimpleDataObjectBuilder(Class<?> type)
        {
            this.type = type;
        }

        SimpleDataObjectImpl build()
        {
            return new SimpleDataObjectImpl(type, map);
        }

        private final Class<?> type;

        private final Map<String, SimpleValueObjectImpl> map = new HashMap<>();
    }

    private static class SimpleValueObjectBuilder
    {
        private SimpleValueObjectBuilder(boolean multiple)
        {
            this.multiple = multiple;
        }

        SimpleValueObjectBuilder key()
        {
            if(multiple)
                throw new DataObjectMalformationException("Found @Key annotation in multiple data object");
            primaryKey = true;
            return this;
        }

        SimpleValueObjectBuilder primaryKey()
        {
            if(!multiple)
                throw new DataObjectMalformationException("Found @PrimaryKey annotation in unique data object");
            primaryKey = true;
            return this;
        }

        SimpleValueObjectBuilder secondaryKey()
        {
            if(!multiple)
                throw new DataObjectMalformationException("Found @SecondaryKey annotation in unique data object");
            secondaryKey = true;
            return this;
        }

        private boolean primaryKey;

        private boolean secondaryKey;

        private final boolean multiple;
    }

    private static class SimpleDataObjectImpl implements DataObject
    {
        private SimpleDataObjectImpl(Class<?> type, Map<String, SimpleValueObjectImpl> map)
        {
            this.type = type;
            this.valueMap = Collections.unmodifiableMap(map);
        }

        @Override
        public Optional<ValueObject> getValue(String name)
        {
            return Optional.ofNullable(valueMap.get(name));
        }

        @Override
        public Map<String, ValueObject> getValues()
        {
            return (Map) valueMap;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        private final Class<?> type;

        final Map<String, SimpleValueObjectImpl> valueMap;
    }

    private static class SimpleValueObjectImpl implements ValueObject
    {
        private SimpleValueObjectImpl(String name,
                                      boolean primary,
                                      boolean secondary,
                                      DataObject owner,
                                      Class<?> ownerType,
                                      Class<?> type,
                                      ExpandRule expandRule,
                                      Getter getter,
                                      Setter setter)
        {
            this.name = name;
            this.primary = primary;
            this.secondary = secondary;
            this.owner = owner;
            this.ownerType = ownerType;
            this.type = type;
            this.expandRule = expandRule;
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        private Object checkReturnType(Object object)
        {
            if(!type.isInstance(object))
                throw new DataObjectError("Incapable return type");

            return object;
        }

        private void checkValue(Object value)
        {
            if(!type.isInstance(value))
                throw DataObjectException.IncapableValue(value, type);
        }

        private void checkType(Class<?> type)
        {
            if(!type.equals(this.type))
                throw DataObjectException.IncapableType(type, this.type);
        }

        private Object get0(Object object)
        {
            Objects.requireNonNull(object, "object");
            return checkReturnType(getter.get(object));
        }

        private void set0(Object object, Object value, boolean check)
        {
            Objects.requireNonNull(object, "object");

            if(check)
                checkValue(value);

            setter.set(object, value);
        }

        @Override
        public Object get(Object object)
        {
            return get0(object);
        }

        @Override
        public <T> T get(Object object, Class<T> type)
        {
            checkType(type);
            return (T) get0(object);
        }

        @Override
        public void set(Object object, Object value)
        {
            set0(object, value, true);
        }

        @Override
        public <T> void set(Object object, T value, Class<T> type)
        {
            checkType(type);
            set0(object, value, false);
        }

        @Override
        public Class<?> getOwnerType()
        {
            return ownerType;
        }

        @Override
        public DataObject getOwner()
        {
            return owner;
        }

        @Override
        public boolean isPrimaryKey()
        {
            return primary;
        }

        @Override
        public boolean isSecondaryKey()
        {
            return secondary;
        }

        @Override
        public Optional<ExpandRule> getExpandRule()
        {
            return Optional.ofNullable(expandRule);
        }

        private final String name;

        private final boolean primary;

        private final boolean secondary;

        private final DataObject owner;

        private final Class<?> ownerType;

        private final Class<?> type;

        private final ExpandRule expandRule;

        private final Getter getter;

        private final Setter setter;
    }

    private static interface Setter
    {
        void set(Object object, Object value);
    }

    private static interface Getter
    {
        Object get(Object object);
    }
}
