package org.kucro3.kraitudao.annotations.expandable;

public @interface CustomExpandRule {
    Class<?> type();

    Entry[] entries();
}
