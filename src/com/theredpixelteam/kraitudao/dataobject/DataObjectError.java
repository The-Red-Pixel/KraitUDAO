package com.theredpixelteam.kraitudao.dataobject;

public class DataObjectError extends Error {
    public DataObjectError()
    {
    }

    public DataObjectError(String msg)
    {
        super(msg);
    }

    public DataObjectError(Throwable cause)
    {
        super(cause);
    }

    public DataObjectError(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
