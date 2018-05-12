package com.theredpixelteam.kraitudao;

import java.util.Optional;

public interface Transaction {
    public boolean push() throws DataSourceException;

    public boolean cancel();

    public Optional<Exception> getLastException();
}
