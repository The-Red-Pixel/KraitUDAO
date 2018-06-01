package com.theredpixelteam.kraitudao.dataobject;

import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;

import java.util.Optional;

public interface DataObjectContainer {
    public Optional<DataObject> get(Class<?> type);

<<<<<<< HEAD
    public default DataObject interpret(Class<?> type, DataObjectInterpreter interpreter)
=======
    public default DataObject get(Class<?> type, DataObjectInterpreter interpreter)
>>>>>>> c91ba8631c42c8d8e5941a753cde97bc4b21aba6
            throws DataObjectInterpretationException
    {
        DataObject dataObject = interpreter.get(type);
        put(type, dataObject);
        return dataObject;
    }

<<<<<<< HEAD
    public default DataObject interpretIfAbsent(Class<?> type, DataObjectInterpreter interpreter)
            throws DataObjectInterpretationException {
        return get(type).orElse(interpret(type, interpreter));
    }

=======
>>>>>>> c91ba8631c42c8d8e5941a753cde97bc4b21aba6
    public boolean remove(Class<?> type);

    public boolean remove(Class<?> type, DataObject dataObject);

    public default boolean contains(Class<?> type)
    {
        return get(type).isPresent();
    }

    public Optional<DataObject> put(Class<?> type, DataObject dataObject);
}
