package verse.cli;

import java.util.Map;

/**
 * Captures all the intent of an instance of a {@link statement}.
 */
public class cmd {

    public final statement statement;
    public final Map<flag, Boolean> flags;
    public final Map<option, String> options;
    public final Iterable<String> rest;

    public cmd(statement statement, Map<flag, Boolean> flags, Map<option, String> options, Iterable<String> rest) {
        this.statement = statement;
        this.flags = flags;
        this.options = options;
        this.rest = rest;
    }
}
