package org.kucro3.kraitudao.dataobject;

import java.util.Map;
import java.util.Optional;

public interface DataObject {
    public boolean hasValue(String name);

    public <T> boolean hasValue(String name, Class<T> type);

    public Optional<ValueObject> getValue(String name);

    public <T> Optional<ValueObject> getValue(String name, Class<T> type);

    public Map<String, ValueObject> getValues();

    public Class<?> getType();
}
