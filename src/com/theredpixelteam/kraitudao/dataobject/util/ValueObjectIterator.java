package com.theredpixelteam.kraitudao.dataobject.util;

import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.redtea.function.Supplier;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ValueObjectIterator implements Iterator<ValueObject>, Iterable<ValueObject> {
    public ValueObjectIterator(DataObject dataObject)
    {
        this.dataObject = dataObject;
        init();
    }

    private void init()
    {
        if(dataObject instanceof ElementDataObject)
            this.node = new Node(() -> dataObject.getValues().values().iterator());
        else if(dataObject instanceof UniqueDataObject)
            (this.node = new Node(() -> Collections.singleton(((UniqueDataObject) dataObject).getKey()).iterator()))
                    .append(() -> dataObject.getValues().values().iterator());
        else if(dataObject instanceof MultipleDataObject)
        {
            (this.node = new Node(() -> Collections.singleton(((MultipleDataObject) dataObject).getPrimaryKey()).iterator()))
                    .append(() -> ((MultipleDataObject) dataObject).getSecondaryKeys().values().iterator())
                    .append(() -> dataObject.getValues().values().iterator());
        }
        else
            throw new IllegalArgumentException("Unsupported data object");
    }

    @Override
    public Iterator<ValueObject> iterator()
    {
        return this;
    }

    @Override
    public boolean hasNext()
    {
        return node.next != null || node.hasNext();
    }

    @Override
    public ValueObject next()
    {
        if(node == null)
            throw new NoSuchElementException();

        if(!node.hasNext())
        {
            node = node.next;
            return next();
        }

        return node.next();
    }

    private Node node;

    protected final DataObject dataObject;

    protected static class Node implements Iterator<ValueObject>
    {
        protected Node(Supplier<Iterator<ValueObject>> supplier)
        {
            this.layzSupplier = supplier;
        }

        void wake()
        {
            this.iterator = layzSupplier.get();
        }

        @Override
        public boolean hasNext()
        {
            if(iterator == null)
                wake();

            return iterator.hasNext();
        }

        @Override
        public ValueObject next()
        {
            if(iterator == null)
                wake();

            return iterator.next();
        }

        Node append(Supplier<Iterator<ValueObject>> supplier)
        {
            Node node = new Node(supplier);
            next = node;
            return node;
        }

        Supplier<Iterator<ValueObject>> layzSupplier;

        Iterator<ValueObject> iterator;

        Node next;
    }
}
