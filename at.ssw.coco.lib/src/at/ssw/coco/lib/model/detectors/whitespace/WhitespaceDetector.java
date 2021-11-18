package at.ssw.coco.lib.model.detectors.whitespace;

public interface WhitespaceDetector {

	/**
	 * Defines Methods to detect whitespace.
	 *
	 * @author Andreas Woess <andwoe@users.sf.net>
	 * @author Andreas Greilinger <Andreas.Greilinger@gmx.net>
	 * @author Konstantin Bina <Konstantin.Bina@gmx.at>
	 */	
	
	public boolean isWhitespace(char ch);

}