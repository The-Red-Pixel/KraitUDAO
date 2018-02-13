package com.theredpixelteam.kraitudao.annotations.expandable;

public @interface Entry {
    String name();

    At getter();

    At setter();

    Class<?> type();
}
