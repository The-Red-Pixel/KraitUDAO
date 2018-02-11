package fun.trp.kraitudao;

public class DataSourceException extends Exception {
    public DataSourceException()
    {
    }

    public DataSourceException(String msg)
    {
        super(msg);
    }

    public DataSourceException(Throwable cause)
    {
        super(cause);
    }

    public DataSourceException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
