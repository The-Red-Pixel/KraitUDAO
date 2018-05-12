package com.theredpixelteam.kraitudao.dataobject;

import com.theredpixelteam.kraitudao.annotations.expandable.Source;

public interface ExpandRule {
    public Class<?> getExpandingType();

    public Entry[] getEntries();

    public interface Entry extends Metadatable {
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
