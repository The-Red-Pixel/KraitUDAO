package com.theredpixelteam.kraitudao.common.sql;

import java.util.Optional;

public interface DataArgumentWrapper {
    public Optional<DataArgument> wrap(Object object);
}
