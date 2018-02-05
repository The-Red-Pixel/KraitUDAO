package org.kucro3.kraitudao.interpreter;

import org.kucro3.kraitudao.dataobject.DataObject;
import org.kucro3.kraitudao.dataobject.MultipleDataObject;
import org.kucro3.kraitudao.dataobject.UniqueDataObject;

public interface DataObjectInterpreter {
    public default DataObject get(Object object) throws DataObjectInterpretationException
    {
        return get(object.getClass());
    }

    public default MultipleDataObject getMultiple(Object object) throws DataObjectInterpretationException
    {
        return getMultiple(object.getClass());
    }

    public default UniqueDataObject getUnique(Object object) throws DataObjectInterpretationException
    {
        return getUnique(object.getClass());
    }

    public DataObject get(Class<?> type) throws DataObjectInterpretationException;

    public MultipleDataObject getMultiple(Class<?> type) throws DataObjectInterpretationException;

    public UniqueDataObject getUnique(Class<?> type) throws DataObjectInterpretationException;
}
