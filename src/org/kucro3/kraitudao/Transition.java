package org.kucro3.kraitudao;

public interface Transition {
    public void push() throws DataSourceException;

    public boolean cancel();
}
