package verse.cli;

import java.util.List;

/**
 * Describes a potential parameter in a {@link statement}.
 */
public class option {

    private List<String> names;

    public List<String> get_names() {
        return names;
    }

    public void set_names(List<String> value) {
        names = value;
    }

    private String placeholder;

    public String get_placeholder() {
        return placeholder;
    }

    public void set_placeholder(String value) {
        placeholder = value;
    }

    private boolean required;

    public boolean get_required() {
        return required;
    }

    public void set_required(boolean value) {
        required = value;
    }

    private boolean repeatable;

    public boolean get_repeatable() {
        return repeatable;
    }

    public void set_repeatable(boolean value) {
        repeatable = value;
    }

    private String descrip;

    public String get_descrip() {
        return descrip;
    }

    public void set_descrip(String value) {
        descrip = value;
    }

    private String _default;

    public String get_default() {
        return _default;
    }

    public void set_default(String value) {
        _default = value;
    }


}
