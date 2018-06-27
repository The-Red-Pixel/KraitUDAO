package com.theredpixelteam.kraitudao.interpreter.common;

import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.kraitudao.interpreter.DataObjectExpander;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectMalformationException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StandardDataObjectExpander implements DataObjectExpander {
    @Override
    public Optional<Map<String, ValueObject>> expand(ValueObject valueObject) throws DataObjectInterpretationException
    {
        ExpandRule expandRule = valueObject.getExpandRule().orElse(null);

        if(expandRule == null)
            return Optional.empty();

        DataObject dataObject = valueObject.getOwner();

        Map<String, ValueObject> map = new HashMap<>();
        for(ExpandRule.Entry entry : expandRule.getEntries())
        {
            Class<?> type = entry.getExpandedType();
            String name = entry.name();

            ExpandedValueObjectContainer valueObjectContainer =
                    new ExpandedValueObjectContainer(dataObject.getType(), type, StandardDataObjectInterpreter.REFLECTION_COMPATIBLE_TYPES.get(type), valueObject);
            valueObjectContainer.name = name;
            valueObjectContainer.primaryKey = valueObject.isPrimaryKey();
            valueObjectContainer.secondaryKey = valueObject.isSecondaryKey();
            valueObjectContainer.owner = valueObject.getOwner();
            valueObjectContainer.metadata = entry.getMetadataMap();

            expand(dataObject, valueObject, valueObjectContainer, entry);

            valueObjectContainer.seal();

            if(dataObject.hasValueObject(name))
                throw new DataObjectMalformationException("Duplicated expand entry: " + name + "(In " + dataObject.getType().getCanonicalName() + ")");

            map.put(valueObjectContainer.getName(), valueObjectContainer);
        }

        return Optional.of(map);
    }

    @Override
    public DataObject expand(DataObject dataObject) throws DataObjectInterpretationException
    {
        StandardDataObjectInterpreter.DataObjectContainer dataObjectContainer;

        if(dataObject instanceof MultipleDataObject)
        {
            MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
            dataObjectContainer = new StandardDataObjectInterpreter.MultipleDataObjectContainer(dataObject.getType());

            dataObjectContainer.putKey(StandardDataObjectInterpreter.KeyType.PRIMARY, multipleDataObject.getPrimaryKey());

            for(ValueObject secondaryKey : multipleDataObject.getSecondaryKeys().values())
            {
                Optional<Map<String, ValueObject>> map = expand(secondaryKey);

                if(!map.isPresent())
                    dataObjectContainer.putKey(StandardDataObjectInterpreter.KeyType.SECONDARY, secondaryKey);
                else for(ValueObject expandedSecondaryKey : map.get().values())
                    dataObjectContainer.putKey(StandardDataObjectInterpreter.KeyType.SECONDARY, expandedSecondaryKey);
            }
        }
        else if(dataObject instanceof UniqueDataObject)
        {
            UniqueDataObject uniqueDataObject = (UniqueDataObject) dataObject;
            dataObjectContainer = new StandardDataObjectInterpreter.UniqueDataObjectContainer(dataObject.getType());

            dataObjectContainer.putKey(StandardDataObjectInterpreter.KeyType.UNIQUE, uniqueDataObject.getKey());
        }
        else
            throw new DataObjectMalformationException.IllegalType();

        for(ValueObject value : dataObject.getValues().values())
        {
            Optional<Map<String, ValueObject>> map = expand(value);

            if (!map.isPresent())
                dataObjectContainer.putValue(value);
            else for(ValueObject expandedValueObject : map.get().values())
                dataObjectContainer.putValue(expandedValueObject);
        }

        return dataObjectContainer;
    }

    private static void expand(DataObject dataObject,
                               ValueObject origin,
                               ExpandedValueObjectContainer valueObjectContainer,
                               ExpandRule.Entry entry)
            throws DataObjectInterpretationException
    {
        Method m0, m1;
        Class<?> type = entry.getExpandedType();

        ExpandRule.At getter = entry.getterInfo();
        ExpandRule.At setter = entry.setterInfo();

        switch(getter.source())
        {
            case THIS:
                try {
                    checkReturnType(m0 = dataObject.getType().getMethod(getter.name(), type), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.getter = (object) -> {
                    try {
                        return m0.invoke(object, origin.get(object));
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;

            case FIELD:
                try {
                    checkReturnType(m0 = origin.getType().getMethod(getter.name()), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.getter = (object) -> {
                    try {
                        return m0.invoke(origin.get(object));
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;
        }

        switch(setter.source())
        {
            case THIS:
                try {
                    m1 = dataObject.getType().getMethod(setter.name(), origin.getType(), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.setter = (object, value) -> {
                    try {
                        m1.invoke(object, origin.get(object), value);
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;

            case FIELD:
                try {
                    m1 = origin.getType().getMethod(setter.name(), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding secondary key (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.setter = (object, value) -> {
                    try {
                        m1.invoke(origin.get(object), value);
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;
        }
    }

    private static void checkReturnType(Method method, Class<?> required) throws NoSuchMethodException
    {
        if(!method.getReturnType().equals(required))
            throw new NoSuchMethodException(String.format("Incompatible return type (Found: %s, Required: %s)",
                    method.getReturnType().getCanonicalName(),
                    required.getCanonicalName()));
    }

    static class ExpandedValueObjectContainer extends StandardDataObjectInterpreter.ValueObjectContainer implements ExpandedValueObject
    {
        ExpandedValueObjectContainer(Class<?> owner, Class<?> type, Class<?> compatibleType, ValueObject source)
        {
            super(owner, type, compatibleType);
            this.source = source;
        }

        @Override
        public ValueObject getSource()
        {
            return source;
        }

        final ValueObject source;
    }

    public static final StandardDataObjectExpander INSTANCE = new StandardDataObjectExpander();
}
