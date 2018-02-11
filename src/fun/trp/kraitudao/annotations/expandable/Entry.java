package fun.trp.kraitudao.annotations.expandable;

public @interface Entry {
    String name();

    At getter();

    At setter();

    Class<?> type();
}
