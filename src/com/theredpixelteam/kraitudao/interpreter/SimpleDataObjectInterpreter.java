package com.theredpixelteam.kraitudao.interpreter;

import com.theredpixelteam.kraitudao.dataobject.*;

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
}
