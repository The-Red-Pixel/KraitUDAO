package fun.trp.kraitudao.dataobject;

import java.util.Optional;

public interface UniqueDataObject extends DataObject {
    public ValueObject getKey();

    public default boolean isKey(String name)
    {
        return getKey(name).isPresent();
    }

    public default Optional<ValueObject> getKey(String name)
    {
        ValueObject object = getKey();

        if(object.getName().equals(name))
            return Optional.of(object);

        return Optional.empty();
    }
}
