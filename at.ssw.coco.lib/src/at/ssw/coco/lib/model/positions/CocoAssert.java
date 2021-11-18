package at.ssw.coco.lib.model.positions;

/**
 * Asserts if a given boolean statement is true.
 *
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public class CocoAssert {
	
	/** Asserts that the given boolean is <code>true</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 *
	 * @param expression the outcode of the check
	 * @return <code>true</code> if the check passes (does not return
	 *    if the check fails)
	 */
	public static boolean isTrue(boolean expression) {
		return isTrue(expression, ""); //$NON-NLS-1$
	}

	/** Asserts that the given boolean is <code>true</code>. If this
	 * is not the case, some kind of unchecked exception is thrown.
	 * The given message is included in that exception, to aid debugging.
	 *
	 * @param expression the outcode of the check
	 * @param message the message to include in the exception
	 * @return <code>true</code> if the check passes (does not return
	 *    if the check fails)
	 */
	
	public static boolean isTrue(boolean expression, String message) {
		if (!expression)
			throw new AssertionFailedException("assertion failed: " + message); //$NON-NLS-1$
		return expression;
	}
}

