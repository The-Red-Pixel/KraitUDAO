package com.theredpixelteam.kraitudao.dataobject;

public class DataObjectMalformationException extends RuntimeException {
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
