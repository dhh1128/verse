package verse.dbc;

/**
 * Enforces that a value is greater than a standard.
 */
public class gt<T extends Comparable<T>> implements constraint<T> {

    /**
     * @return Value on right-hand side of a comparison (the standard
     * against which left-hand side will be compared).
     */
    public final T rhs;

    public gt(T rhs) {
        this.rhs = rhs;
    }

    @Override
    public boolean satisfied_by(T value) {
        return value.compareTo(rhs) > 0;
    }
}
