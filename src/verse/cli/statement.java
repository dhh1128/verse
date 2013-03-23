package verse.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes the syntax of a potential {@link cmd}.
 */
public class statement {

    public final String name;

    public statement(String name) {
        this.name = name;
    }

    public List<flag> flags;

    public List<flag> get_flags() {
        return flags;
    }

    public void add_flag(String... names) throws invalid_menu_exception {
        if (flags == null) {
            flags = new ArrayList<flag>();
        }
        flags.add(new flag(names));
    }

    public List<option> options;

    public List<option> get_options() {
        return options;
    }

    public void set_options(List<option> value) {
        options = value;
    }

    public void validate() throws invalid_menu_exception {
        for (flag flg: flags) {

        }
        for (option opt: options) {

        }
    }

    public cmd parse(String[] args) throws invalid_cmd_exception {
        return null;
    }
}
