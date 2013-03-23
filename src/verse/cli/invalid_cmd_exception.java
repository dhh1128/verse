package verse.cli;

/**
 * Describes syntax errors in a command.
 */
public class invalid_cmd_exception extends Exception {

    public invalid_cmd_exception(String msg) {
        super(msg);
    }

}
