package org.kucro3.kraitudao.interpreter;

import org.kucro3.kraitudao.annotations.Multiple;
import org.kucro3.kraitudao.annotations.Unique;
import org.kucro3.kraitudao.dataobject.DataObject;
import org.kucro3.kraitudao.dataobject.MultipleDataObject;
import org.kucro3.kraitudao.dataobject.UniqueDataObject;

import java.util.HashMap;
import java.util.Map;

public class SimpleDataObjectInterpreter implements DataObjectInterpreter {
    @Override
    public DataObject get(Class<?> type) throws DataObjectInterpretationException
    {
        if(isMultiple(type))
            return getMultiple0(type);
        else if(isUnique(type))
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

    private final Map<Class<?>, DataObject> cached = new HashMap<>();
}
