package verse.dbc;

import java.util.regex.Pattern;

/**
 * Enforces that a string value contains a regex.
 */
public class search implements constraint<String> {
    public search(Pattern regex) {
        precondition.checkNotNull(regex, "regex");
        rhs = regex;
    }

    private Pattern rhs;

    /**
     * @return Regex against which string on left-hand side will be compared.
     */
    public Pattern get_rhs() {
        return rhs;
    }

    @Override
    public boolean satisfied_by(String value) {
        return rhs.matcher(value).find();
    }
}
