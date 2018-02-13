package com.theredpixelteam.kraitudao.interpreter;

import com.theredpixelteam.kraitudao.annotations.Multiple;
import com.theredpixelteam.kraitudao.annotations.Unique;
import com.theredpixelteam.kraitudao.annotations.expandable.At;
import com.theredpixelteam.kraitudao.annotations.expandable.Source;
import com.theredpixelteam.kraitudao.dataobject.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        private SimpleDataObjectBuilder(Set<String> names, Class<?> type)
        {
            this.names = names;
            this.type = type;
        }

        SimpleDataObjectBuilder valueObject(ValueObject valueObject)
        {
            if(!names.add(valueObject.getName()))
                throw new DataObjectMalformationException("Duplicated value object name");
            this.map.put(valueObject.getName(), valueObject);
            return this;
        }

        SimpleDataObjectImpl build()
        {
            return new SimpleDataObjectImpl(type, map);
        }

        private Boolean multiple;

        private final Class<?> type;

        private final Set<String> names;

        private final Map<String, ValueObject> map = new HashMap<>();
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

        SimpleValueObjectBuilder type(Class<?> type)
        {
            this.type = type;
            return this;
        }

        SimpleValueObjectBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        SimpleValueObjectBuilder owner(DataObject owner)
        {
            this.owner = owner;
            return this;
        }

        SimpleValueObjectBuilder ownerType(Class<?> ownerType)
        {
            this.ownerType = ownerType;
            return this;
        }

        SimpleValueObjectBuilder getter(Getter getter)
        {
            this.getter = getter;
            return this;
        }

        SimpleValueObjectBuilder setter(Setter setter)
        {
            this.setter = setter;
            return this;
        }

        SimpleValueObjectBuilder expandRule(ExpandRule rule)
        {
            this.expandRule = rule;
            return this;
        }

        ValueObject build()
        {
            return new SimpleValueObjectImpl(
                    name,
                    primaryKey,
                    secondaryKey,
                    owner,
                    ownerType,
                    type,
                    expandRule,
                    getter,
                    setter
            );
        }

        private ExpandRule expandRule;

        private String name;

        private Class<?> type;

        private DataObject owner;

        private Class<?> ownerType;

        private boolean primaryKey;

        private boolean secondaryKey;

        private Getter getter;

        private Setter setter;

        private final boolean multiple;
    }

    private static class SimpleExpandRuleBuilder
    {
        private SimpleExpandRuleBuilder(Set<String> names)
        {
            this.names = names;
        }

        SimpleExpandRuleBuilder expandingType(Class<?> expandingType)
        {
            this.expandingType = expandingType;
            return this;
        }

        SimpleExpandRuleBuilder entry(ExpandRule.Entry entry)
        {
            if(!names.add(entry.name()))
                throw new DataObjectMalformationException("Duplicated value name: " + entry.name());
            this.entires.add(entry);
            return this;
        }

        ExpandRule build()
        {
            return new SimpleExpandRuleImpl(expandingType, entires.toArray(new ExpandRule.Entry[entires.size()]));
        }

        private final Set<String> names;

        private final List<ExpandRule.Entry> entires = new ArrayList<>();

        private Class<?> expandingType;
    }

    private static class SimpleExpandRuleEntryBuilder
    {
        SimpleExpandRuleEntryBuilder type(Class<?> type)
        {
            this.type = type;
            return this;
        }

        SimpleExpandRuleEntryBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        SimpleExpandRuleEntryBuilder getter(At getter)
        {
            this.getter = getter;
            return this;
        }

        SimpleExpandRuleEntryBuilder setter(At setter)
        {
            this.setter = setter;
            return this;
        }

        ExpandRule.Entry build()
        {
            return new SimpleExpandRuleEntryImpl(name, type, getter, setter);
        }

        private At getter;

        private At setter;

        private Class<?> type;

        private String name;
    }

    private static class SimpleExpandRuleImpl implements ExpandRule
    {
        private SimpleExpandRuleImpl(Class<?> expandingType, Entry[] entries)
        {
            this.expandingType = expandingType;
            this.entries = entries;
        }

        @Override
        public Class<?> getExpandingType()
        {
            return expandingType;
        }

        @Override
        public Entry[] getEntries()
        {
            return Arrays.copyOf(entries, entries.length);
        }

        private final Entry[] entries;

        private final Class<?> expandingType;
    }

    private static class SimpleExpandRuleEntryImpl implements ExpandRule.Entry
    {
        private SimpleExpandRuleEntryImpl(String name,
                                          Class<?> type,
                                          At getter,
                                          At setter)
        {
            this(name, type, new SimpleAtInfoImpl(getter), new SimpleAtInfoImpl(setter));
        }

        private SimpleExpandRuleEntryImpl(String name,
                                          Class<?> type,
                                          ExpandRule.At getter,
                                          ExpandRule.At setter)
        {
            this.name = name;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String name()
        {
            return name;
        }

        @Override
        public ExpandRule.At getterInfo()
        {
            return getter;
        }

        @Override
        public ExpandRule.At setterInfo()
        {
            return setter;
        }

        @Override
        public Class<?> getExpandedType()
        {
            return type;
        }

        private final String name;

        private final Class<?> type;

        private final ExpandRule.At getter;

        private final ExpandRule.At setter;

        private static class SimpleAtInfoImpl implements ExpandRule.At
        {
            private SimpleAtInfoImpl(At info)
            {
                this.info = info;
            }

            @Override
            public String name()
            {
                return info.name();
            }

            @Override
            public Source source()
            {
                return info.source();
            }

            private final At info;
        }
    }

    private static class SimpleDataObjectImpl implements DataObject
    {
        private SimpleDataObjectImpl(Class<?> type, Map<String, ValueObject> map)
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
            return valueMap;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        private final Class<?> type;

        final Map<String, ValueObject> valueMap;
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

    private static class FieldGetter implements Getter
    {
        private FieldGetter(Field field)
        {
            this.field = field;
        }

        @Override
        public Object get(Object object)
        {
            try {
                return field.get(object);
            } catch (Exception e) {
                throw new DataObjectError(e);
            }
        }

        private final Field field;
    }

    private static class FieldSetter implements Setter
    {
        private FieldSetter(Field field)
        {
            this.field = field;
        }

        @Override
        public void set(Object object, Object value)
        {
            try {
                field.set(object, value);
            } catch (Exception e) {
                throw new DataObjectException(e);
            }
        }

        private final Field field;
    }

    private static class MethodGetter implements Getter
    {
        private MethodGetter(Method method)
        {
            this.method = method;
        }

        @Override
        public Object get(Object object)
        {
            try {
                return method.invoke(object);
            } catch (Exception e) {
                throw new DataObjectException(e);
            }
        }

        private final Method method;
    }

    private static class MethodSetter implements Setter
    {
        private MethodSetter(Method method)
        {
            this.method = method;
        }

        @Override
        public void set(Object object, Object value)
        {
            try {
                method.invoke(object, value);
            } catch (Exception e) {
                throw new DataObjectException(e);
            }
        }

        private final Method method;
    }
}
