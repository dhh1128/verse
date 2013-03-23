package verse.dbc;

import java.util.regex.Pattern;

/**
 * Enforces that a string value matches a regex.
 */
public class match implements constraint<String> {
    public match(Pattern regex) {
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
        return rhs.matcher(value).matches();
    }
}
