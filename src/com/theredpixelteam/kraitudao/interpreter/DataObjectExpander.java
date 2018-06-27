package com.theredpixelteam.kraitudao.interpreter;

import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.dataobject.ValueObject;

import java.util.Map;
import java.util.Optional;

public interface DataObjectExpander {
    public Optional<Map<String, ValueObject>> expand(ValueObject valueObject) throws DataObjectInterpretationException;

    public DataObject expand(DataObject dataObject) throws DataObjectInterpretationException;
}
