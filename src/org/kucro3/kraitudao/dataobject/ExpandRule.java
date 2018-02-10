package org.kucro3.kraitudao.dataobject;

import org.kucro3.kraitudao.annotations.expandable.Source;

public interface ExpandRule {
    public Class<?> getExpandingType();

    public Entry[] getEntries();

    public interface Entry {
        public String name();

        public At getterInfo();

        public At setterInfo();

        public Class<?> getExpandedType();
    }

    public interface At {
        public String name();

        public Source source();
    }
}
