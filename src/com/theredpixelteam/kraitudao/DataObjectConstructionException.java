package com.theredpixelteam.kraitudao;

public class DataObjectConstructionException extends DataSourceException {
    public DataObjectConstructionException()
    {
    }

    public DataObjectConstructionException(String msg)
    {
        super(msg);
    }

    public DataObjectConstructionException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectConstructionException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
