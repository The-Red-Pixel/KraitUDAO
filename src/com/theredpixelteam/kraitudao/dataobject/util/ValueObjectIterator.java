/*
 * ValueObjectIterator.java
 *
 * Copyright (C) 2018 The Red Pixel <theredpixelteam.com>
 * Copyright (C) 2018 KuCrO3 Studio <kucro3.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

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
