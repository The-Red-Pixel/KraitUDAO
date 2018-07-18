/*
 * ObjectConstructor.java
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

package com.theredpixelteam.kraitudao;

import com.theredpixelteam.redtea.function.FunctionWithThrowable;
import com.theredpixelteam.redtea.util.Optional;

public interface ObjectConstructor<T> {
    public T newInstance(Object object) throws Exception;

    public Class<T> getType();

    @SuppressWarnings("unchecked")
    public default <R> Optional<ObjectConstructor<R>> as(Class<R> type)
    {
        if(type.isAssignableFrom(getType()))
            return Optional.of((ObjectConstructor<R>) this);
        return Optional.empty();
    }

    public static <T> ObjectConstructor<T> of(Class<T> type, FunctionWithThrowable<Object, T, Exception> supplier)
    {
        return new ObjectConstructor<T>() {
            @Override
            public T newInstance(Object object) throws Exception
            {
                return supplier.apply(object);
            }

            @Override
            public Class<T> getType()
            {
                return type;
            }
        };
    }

    public static <T> ObjectConstructor<T> ofDefault(Class<T> type)
    {
        return of(type, (obj) -> type.newInstance());
    }
}
