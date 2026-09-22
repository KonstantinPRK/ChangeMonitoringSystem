package infrastructure;

public interface Sourcer<I, O> {
    O process(I message);

    default void close() {
    }
}
