package verse.dbc;

/**
 * Interface for all constraints used to extend semantics of a variable.
 */
public interface constraint<T> {
    boolean satisfied_by(T value);
}
