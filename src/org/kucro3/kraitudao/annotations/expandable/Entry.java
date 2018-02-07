package org.kucro3.kraitudao.annotations.expandable;

public @interface Entry {
    String name();

    At getter();

    At setter();

    Class<?> type();
}
