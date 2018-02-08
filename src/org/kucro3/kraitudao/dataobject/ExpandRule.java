package org.kucro3.kraitudao.dataobject;

import org.kucro3.kraitudao.annotations.expandable.At;

public interface ExpandRule {
    public Class<?> getExpandingType();

    public interface Entry {
        public String name();

        public At getterInfo();

        public At setterInfo();

        public Class<?> getExpandedType();
    }
}
