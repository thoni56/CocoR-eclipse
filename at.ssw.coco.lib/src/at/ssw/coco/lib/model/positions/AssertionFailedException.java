package at.ssw.coco.lib.model.positions;

/**
 * Defines an AssertionFailedException
 *
 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
 */

public class AssertionFailedException extends RuntimeException {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * Constructs a new exception with the given message.
	 * 
	 * @param detail the message
	 */
	public AssertionFailedException(String detail) {
		super(detail);
	}
}
