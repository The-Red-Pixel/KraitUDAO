package com.theredpixelteam.kraitudao.annotations.expandable;

public @interface CustomExpandRule {
    Class<?> type();

    Entry[] entries();
}
