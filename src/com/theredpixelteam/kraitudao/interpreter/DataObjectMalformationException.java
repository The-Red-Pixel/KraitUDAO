package com.theredpixelteam.kraitudao.interpreter;

public class DataObjectMalformationException extends DataObjectInterpretationException {
    public DataObjectMalformationException()
    {
    }

    public DataObjectMalformationException(String msg)
    {
        super(msg);
    }

    public DataObjectMalformationException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectMalformationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
