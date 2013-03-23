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

/**
 * <p>
 * Same basic functionality as {@link precondition} and {@link condition},
 * but designed to enforce integrity checks invisible to caller. At the end of a
 * complex function, write postconditions to verify that you're leaving the
 * function with correct state.
 * </p>
 * <p>
 * Postconditions test for correct <i>implementation</i>; they do <i>not</i>
 * test that a function is called correctly.
 * </p>
 */
public class postcondition extends condition {
	/**
	 * Test a contract and throw an {@link contract_violation} if it fails. The
	 * overload {@link #checkAndExplain} provides better diagnostic messages.
	 * 
	 * @param value
	 *            The value to test.
	 */
	public static void check(boolean value) {
		check(postcondition.class, value, 1);
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
		checkAndExplain(postcondition.class, value, contract, args, 1);
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
		checkNotNull(postcondition.class, o, expr, 1);
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
		checkNotNullOrEmpty(postcondition.class, text, expr, 1);
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
		checkNotNullOrEmpty(postcondition.class, array, expr, 1);
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
		checkNotNullOrEmpty(postcondition.class, collection, expr, 1);
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
        condition.checkLineCount(postcondition.class, value, minLines, maxLines, name, 1);
    }
}
