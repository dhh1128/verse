import verse.cli.cmd;
import verse.cli.invalid_menu_exception;
import verse.cli.menu;
import verse.cli.statement;

/**
 * Run the verse compiler.
 */
public class app {
    public static void main(String[] args) {
        try {
            cmd cmd = menu.parse(args);
            if (cmd.statement.name == "help") {
                System.out.print(menu.get_help());
                System.exit(0);
            }


        } catch (Throwable t) {
            print(t);
            fail(-1);
        }
    }

    public static void fail(int exit_code) {
        System.exit(exit_code);
    }

    public static void print(Throwable t) {
        System.err.print(t.toString());
    }

    private static menu menu;
    static {
        menu m = new menu("verse", "Compile a verse code.", null, menu.global_help);
        try {
            m.validate();
        } catch (invalid_menu_exception e) {
            System.err.println(e.toString());
            System.exit(-1);
        }
        menu = m;
    }
}
