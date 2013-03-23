package verse.cli;

import verse.util.str_util;

import java.io.PrintStream;
import java.util.List;

/**
 * Contains an inventory of all recognizable {@link statement}s, and
 * describes the relationship between them.
 */
public class menu {

    public final String name;
    public final String descrip;
    public final String epilogue;

    /**
     * Add this statement to your menu if you intend to support a cmdline syntax where the user
     * can type "--help", "-h", or "-?" without other args.
     */
    public static final statement global_help;

    /**
     * Add this statement to your menu if you intend to support a cmdline syntax where the user
     * can type "help &lt;something&gt;" to get help on more commands.
     */
    public static final statement cmd_help;

    static {
        statement gh = new statement("help");
        statement ch = new statement("help on command");
        try {
            gh.add_flag("help", "h", "?");
        } catch (invalid_menu_exception e) {
            System.err.println(e.toString());
            System.exit(-1);
        }
        global_help = gh;
        cmd_help = null;
    }

    public menu(String name, String descrip, String epilogue, statement... statements) {
        this.name = name;
        this.descrip = descrip;
        this.epilogue = epilogue;
        this.statements = statements;
    }

    public final statement[] statements;

    public void validate() throws invalid_menu_exception {
        invalid_menu_exception ex = null;
        for (statement stmt: statements) {
            try {
                stmt.validate();
            } catch (invalid_menu_exception e) {
                if (ex == null) {
                    ex = new invalid_menu_exception();
                    ex.add_cause(e);
                }
            }
        }
        if (ex.get_causes() != null) {
            throw ex;
        }
    }

    public cmd parse(String[] args) throws invalid_cmd_exception {
        cmd cmd = null;
        for (statement stmt: statements) {
            cmd = stmt.parse(args);
            if (cmd != null) {
                return cmd;
            }
        }
        throw new invalid_cmd_exception("Command did not match any statement in the menu.");
    }

    private static String LINE_SEP = System.getProperty("line.separator");

    private void describe_statement(StringBuilder sb, statement statement) {
        sb.append(statement.name);
        sb.append(LINE_SEP);
        sb.append("  ");
        sb.append(name);
        sb.append(' ');
        for (flag flg: statement.flags) {
            String name = flg.names[0];
            sb.append('[');
            if (name.length() > 1) {
                sb.append("--");
            } else {
                sb.append('-');
            }
            sb.append(name);
            sb.append(']');
        }
        for (option opt: statement.options) {
            String name = "opt"; //fix
            sb.append("option ");
        }
    }

    public String get_help() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" -- ");
        sb.append(descrip);
        sb.append(LINE_SEP);
        sb.append(LINE_SEP);
        for (statement stmt: statements) {
            describe_statement(sb, stmt);
        }
        if (!str_util.is_null_or_empty(epilogue)) {
            sb.append(LINE_SEP);
            sb.append(epilogue);
        }
        sb.append(LINE_SEP);
        return sb.toString();
    }
}
