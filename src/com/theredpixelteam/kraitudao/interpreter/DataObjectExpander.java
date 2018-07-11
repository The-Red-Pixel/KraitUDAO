package com.theredpixelteam.kraitudao.interpreter;

import com.theredpixelteam.kraitudao.dataobject.DataObject;
import com.theredpixelteam.kraitudao.dataobject.ValueObject;
import com.theredpixelteam.redtea.util.Optional;

import java.util.Map;

public interface DataObjectExpander {
    public Optional<Map<String, ValueObject>> expand(ValueObject valueObject) throws DataObjectInterpretationException;

    public DataObject expand(DataObject dataObject) throws DataObjectInterpretationException;
}
