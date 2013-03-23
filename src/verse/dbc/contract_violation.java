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
 * An error that describes how a contract was violated. See {@link precondition}
 * , {@link condition}, {@link postcondition}, and <a
 * href="http://en.wikipedia.org/wiki/Design_by_contract">the wikipedia
 * article</a> about design by contract).
 */
public class contract_violation extends AssertionError {

	/**
	 * Short message of violation
	 */
	private String shortMessage = "";
		
	contract_violation(Class<?> contractType, String msg,
                       StackTraceElement[] stackTrace, int unwindLevels) {
		super(buildMessage(contractType.getSimpleName(), msg, stackTrace, 
				unwindLevels));
		mContractType = contractType;
		trimStackTrace(stackTrace, unwindLevels);
		
		this.shortMessage = buildShortMessage(contractType.getSimpleName(), msg);
	}
	
	private final Class<?> mContractType;

	/**
	 * @return a class indicating what type of contract was violated --
	 *         {@link precondition}, {@link condition}, or {@link
	 *         postcondition}.
	 */
	public Class<?> getContractType() {
		return mContractType;
	}

	/**
	 * @return a {@link StackTraceElement} identifying where the contract
	 *         violation was detected.
	 */
	public StackTraceElement getSite() {
		return getStackTraceElement(0);
	}

	/**
	 * @return a {@link StackTraceElement} identifying the caller that invoked
	 *         the function where the contract violation was detected. This is
	 *         useful for {@link precondition}s, because the error was caused by
	 *         the caller. It is not useful for {@link postcondition}s.
	 */
	public StackTraceElement getCaller() {
		return getStackTraceElement(1);
	}

	private StackTraceElement getStackTraceElement(int i) {
		StackTraceElement[] trace = getStackTrace();
		return (trace != null && i < trace.length) ? trace[i] : null;
	}

	private void trimStackTrace(StackTraceElement[] stackTrace, int unwindLevels) {
		if (unwindLevels < stackTrace.length) {
			StackTraceElement[] trace = new StackTraceElement[stackTrace.length
					- unwindLevels];
			for (int i = 0, j = unwindLevels; i < trace.length; ++i, ++j) {
				trace[i] = stackTrace[j];
			}
			setStackTrace(trace);
		}
	}

	private static String buildMessage(String contractType, String msg,
			StackTraceElement[] stackTrace, int unwindLevels) {
		StringBuilder sb = new StringBuilder();
		sb.append(contractType);
		sb.append(' ');
		sb.append("failed");
		if (stackTrace != null && stackTrace.length > 0
				&& stackTrace[unwindLevels] != null) {
			sb.append(" at ");
			sb.append(stackTrace[unwindLevels].toString());
			if (contractType.startsWith("Pre")) {
				sb.append(", indicating incorrect use of the function by its caller");
				if (stackTrace.length > unwindLevels + 1 && 
						stackTrace[unwindLevels + 1] != null) {
					sb.append(", ");
					sb.append(stackTrace[unwindLevels + 1].toString());
				}
			} else {
				sb.append(", indicating an internal algorithm with unexpected behavior");
			}
			sb.append('.');
		}
		if (msg != null) {
			sb.append(' ');
			sb.append(msg);
		}
		return sb.toString();
	}
	
	/**
	 * @return A string with violation description without stack information. 
	 *         This description can be used to show error for user.  
	 * @param contractType
	 *            Name of the violated contract
	 *        msg
	 *            Violation message
	 */
	private static String buildShortMessage(String contractType, String msg){
		StringBuilder shortSb = new StringBuilder();
		shortSb
			.append(contractType)
			.append(" failed with message: ")
			.append(msg);
		
		return shortSb.toString();
	}

	/**
	 * @return A string with violation description without stack information. 
	 *         This description can be used to show error for user.
	 */
	public String getShortMessage() {
		return this.shortMessage;
	}	

	private static final long serialVersionUID = -7451423601257013473L;
}
