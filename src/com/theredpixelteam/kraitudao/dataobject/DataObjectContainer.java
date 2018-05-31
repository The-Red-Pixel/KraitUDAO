package com.theredpixelteam.kraitudao.dataobject;

import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;

import java.util.Optional;

public interface DataObjectContainer {
    public Optional<DataObject> get(Class<?> type);

    public default DataObject interpret(Class<?> type, DataObjectInterpreter interpreter)
            throws DataObjectInterpretationException
    {
        DataObject dataObject = interpreter.get(type);
        put(type, dataObject);
        return dataObject;
    }

    public default DataObject interpretIfAbsent(Class<?> type, DataObjectInterpreter interpreter)
            throws DataObjectInterpretationException {
        return get(type).orElse(interpret(type, interpreter));
    }

    public boolean remove(Class<?> type);

    public boolean remove(Class<?> type, DataObject dataObject);

    public default boolean contains(Class<?> type)
    {
        return get(type).isPresent();
    }

    public Optional<DataObject> put(Class<?> type, DataObject dataObject);
}
