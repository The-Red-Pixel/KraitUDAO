package fun.trp.kraitudao.annotations.expandable;

public @interface CustomExpandRule {
    Class<?> type();

    Entry[] entries();
}
