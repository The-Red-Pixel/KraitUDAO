package fun.trp.kraitudao;

public interface Transition {
    public void push() throws DataSourceException;

    public boolean cancel();
}
