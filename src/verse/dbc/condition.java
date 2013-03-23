/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 16, 2009
 */
package verse.dbc;

import verse.util.str_util;

/**
 * <p>
 * Support for design by contract (see <a
 * href="http://en.wikipedia.org/wiki/Design_by_contract">the wikipedia
 * article</a>).
 * </p>
 * 
 * <p>
 * Contract checks are like assertions, but they provide better semantics, and
 * they do not get turned off based on JVM configuration. Many constraints that
 * can be represented as contract checks are sooner or later enforced by java
 * itself, eventually causing a {@link NullPointerException} or an
 * {@link ArrayIndexOutOfBoundsException} if a function receives invalid input
 * or behaves badly. However, particularly on public APIs, it may be desirable
 * to formally enforce contracts so that:
 * </p>
 * 
 * <ol>
 * <li>Problems are reported in a standard, friendly way, and with a single
 * exception type (see {@link contract_violation}).</li>
 * <li>Unpredictable behavior is aborted immediately, thus saving execution
 * time, preventing side-effects, and guaranteeing relevant error messages/stack
 * traces.</li>
 * </ol>
 * 
 * <p>
 * It is important to note that these reasons apply equally to debug versus
 * release code; hence contract checks are not disable-able like assertions.
 * </p>
 * 
 * <p>
 * Contract checks come in three varieties:
 * </p>
 * <dl>
 * <dt>{@link precondition}</dt>
 * <dd>Used to guarantee that callers pass valid parameters.</dd>
 * <dt>{@link condition}</dt>
 * <dd>Used to guarantee that a called function worked as advertised.</dd>
 * <dt>{@link postcondition}</dt>
 * <dd>Used to guarantee that an implementation is coded correctly.</dd>
 * </dl>
 * 
 * <p>
 * Contract checks that pass are quite cheap (the cost of evaluating a boolean
 * expression); as a best practice, any computation performed as input to the
 * boolean expression should also be cheap. In other words, it's not a good idea
 * to shuffle and sort an array of a million <code>int</code>s in a
 * precondition. Also, like assertions, contract checks are intended to be
 * semantically idempotent; they should not modify system state by virtue of
 * their being called.
 * </p>
 */
public class condition {

	/**
	 * Test a contract and throw an {@link contract_violation} if it fails. The
	 * overload {@link #checkAndExplain} provides better diagnostic messages.
	 * 
	 * @param value
	 *            The value to test.
	 */
	public static void check(boolean value) {
		check(condition.class, value, 1);
	}

	/**
	 * Test a contract and throw an {@link contract_violation} if it fails.
	 * 
	 * @param value
	 *            The value to test.
	 * @param contract
	 *            A brief phrase that describes the contract -- for example,
	 *            "a date after September 2009". This phrase is used to
	 *            construct the contract_violation message if one is raised. The
	 *            phrase should <i>not</i> be capitalized or punctuated as a
	 *            complete sentence.
	 * @param args
	 *            Zero or more args that are used to expand format specifiers
	 *            inside <code>expected</code>.
	 */
	public static void checkAndExplain(boolean value, String contract,
			Object... args) {
		checkAndExplain(condition.class, value, contract, args, 1);
	}

	/**
	 * Test that an object is not null.
	 * 
	 * @param o
	 *            The object to test.
	 * @param expr
	 *            The expression (e.g., name of the parameter) that's being
	 *            tested.
	 */
	public static void checkNotNull(Object o, String expr) {
		checkNotNull(condition.class, o, expr, 1);
	}

	/**
	 * Test that a string or char[] is not null or empty.
	 * 
	 * @param text
	 *            The string or char[] to test.
	 * @param expr
	 *            The expression (e.g., name of the parameter) that's being
	 *            tested.
	 */
	public static void checkNotNullOrEmpty(CharSequence text, String expr) {
		checkNotNullOrEmpty(condition.class, text, expr, 1);
	}

	/**
	 * Test that an array is not null or empty.
	 * 
	 * @param array
	 *            The array to test.
	 * @param expr
	 *            The expression (e.g., name of the parameter) that's being
	 *            tested.
	 */
	public static void checkNotNullOrEmpty(Object[] array, String expr) {
		checkNotNullOrEmpty(condition.class, array, expr, 1);
	}

	/**
	 * Test that a collection is not null or empty.
	 * 
	 * @param collection
	 *            The collection to test.
	 * @param expr
	 *            The expression (e.g., name of the parameter) that's being
	 *            tested.
	 */
	public static void checkNotNullOrEmpty(Iterable<?> collection, String expr) {
		checkNotNullOrEmpty(condition.class, collection, expr, 1);
	}

    /**
     * Test that a string has the right number of lines.
     *
     * @param value
     *            The value to test.
     * @param minLines
     *            What is the minimum number of acceptable lines, inclusive?
     * @param maxLines
     *            What is the maximum number of acceptable lines, inclusive?
     * @param name
     *            Name of the variable under test.
     */
    public static void checkLineCount(String value, int minLines, int maxLines, String name) {
        condition.checkLineCount(condition.class, value, minLines, maxLines, name, 1);
    }

    private static final String SHOULDNT_BE_EMPTY = "%s should not be null/empty";

	protected static void checkNotNullOrEmpty(Class<?> contractType,
			CharSequence text, String expr, int unwindLevel) {
		if (text == null || text.length() == 0) {
			fail(contractType, unwindLevel + 1, SHOULDNT_BE_EMPTY,
					new Object[] { expr });
		}
	}

	protected static void checkNotNullOrEmpty(Class<?> contractType,
			Object[] array, String expr, int unwindLevel) {
		if (array == null || array.length == 0) {
			fail(contractType, unwindLevel + 1, SHOULDNT_BE_EMPTY,
					new Object[] { expr });
		}
	}

	protected static void checkNotNullOrEmpty(Class<?> contractType,
			Iterable<?> obj, String expr, int unwindLevel) {
		if (obj == null || !obj.iterator().hasNext()) {
			fail(contractType, unwindLevel + 1, SHOULDNT_BE_EMPTY,
					new Object[] { expr });
		}
	}

	protected static void check(Class<?> contractType, boolean value,
			int unwindLevels) {
		if (!value) {
			fail(contractType, unwindLevels + 1, null, null);
		}
	}

	protected static void checkAndExplain(Class<?> contractType, boolean value,
			String contract, Object[] args, int unwindLevels) {
		if (!value) {
			fail(contractType, unwindLevels + 1, contract, args);
		}
	}

	private static final String SHOULDNT_BE_NULL = "%s should not be null";

	protected static void checkNotNull(Class<?> contractType, Object o,
			String expr, int unwindLevels) {
		if (o == null) {
			fail(contractType, unwindLevels + 1, SHOULDNT_BE_NULL, new Object[] { expr });
		}
	}

    /**
     * Test a contract and throw an {@link contract_violation} if it fails.
     *
     * @param value
     *            The value to test.
     * @param minLines
     *            What is the minimum number of acceptable lines, inclusive?
     * @param maxLines
     *            What is the maximum number of acceptable lines, inclusive?
     * @param name
     *            Name of the variable under test.
     */
    protected static void checkLineCount(Class<?> contractType, String value, int minLines, int maxLines, String name, int unwindLevels) {
        int lc = str_util.count_complete_lines(value) + 1;
        if (lc < minLines || lc > maxLines) {
            String msg;
            if (minLines == maxLines) {
                String quant = (minLines == 1) ? "line" : "lines";
                msg = String.format("%s should be %d %s, not %d", name, minLines, quant, lc);
            } else {
                msg = String.format("%s should be from %d to %d lines, not %d", name, minLines, maxLines, lc);
            }
            fail(contractType, unwindLevels + 1, msg, null);
        }
    }

    protected static void fail(Class<?> contractType, int unwindLevels,
			String msg, Object[] args) {
		StackTraceElement[] trace = new Throwable().getStackTrace();
		msg = (msg != null && !msg.isEmpty()) ? String.format(msg, args) : "";
		throw new contract_violation(contractType, msg, trace, unwindLevels + 1);
	}
}
