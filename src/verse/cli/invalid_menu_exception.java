package verse.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes logical errors in menu.
 */
public class invalid_menu_exception extends Exception {

    public invalid_menu_exception() {
    }

    List<invalid_menu_exception> causes;

    List<invalid_menu_exception> get_causes() {
        return causes;
    }

    void add_cause(invalid_menu_exception cause) {
        if (causes == null) {
            causes = new ArrayList<invalid_menu_exception>();
        }
        causes.add(cause);
    }
}
