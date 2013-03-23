package verse.cli;

import verse.dbc.precondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a binary option in a statement. When the flag is missing, its value
 * is false; when present, its value is true.
 */
public class flag {

    public final String[] names;

    public flag(String... names) throws invalid_menu_exception {
        for (String name: names) {
            precondition.checkNotNullOrEmpty(name, "name");
            precondition.checkLineCount(name, 1, 1, "name");
            precondition.checkAndExplain(name.length() <= 32, "length of flag and option names cannot exceed 32 chars");
        }
        this.names = names;
    }


}
