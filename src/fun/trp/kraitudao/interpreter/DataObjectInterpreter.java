package fun.trp.kraitudao.interpreter;

import fun.trp.kraitudao.dataobject.DataObject;
import fun.trp.kraitudao.dataobject.MultipleDataObject;
import fun.trp.kraitudao.dataobject.UniqueDataObject;

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
