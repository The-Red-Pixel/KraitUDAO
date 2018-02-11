package fun.trp.kraitudao.interpreter;

public class DataObjectInterpretationException extends Exception {
    public DataObjectInterpretationException()
    {
    }

    public DataObjectInterpretationException(String msg)
    {
        super(msg);
    }

    public DataObjectInterpretationException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectInterpretationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
